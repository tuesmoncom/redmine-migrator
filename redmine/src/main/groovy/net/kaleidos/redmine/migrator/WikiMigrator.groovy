package net.kaleidos.redmine.migrator

import com.taskadapter.redmineapi.bean.WikiPage as RedmineWikiPage
import com.taskadapter.redmineapi.bean.WikiPageDetail as RedmineWikiPageDetail

import groovy.util.logging.Log4j
import groovy.transform.InheritConstructors

import java.text.Normalizer
import java.util.regex.Pattern

import net.kaleidos.domain.Wikipage as TuesmonWikiPage
import net.kaleidos.domain.Attachment as TuesmonAttachment
import net.kaleidos.redmine.RedmineTuesmonRef

@Log4j
@InheritConstructors
class WikiMigrator extends AbstractMigrator<TuesmonWikiPage> {

    static final String REGEX1 = /\[\[(\s*\p{L}+(?:\s+\p{L}+)*\s*)\]\]/
    static final String REGEX2 = /\[\[(\s*\p{L}+(?:\s+\p{L}+)*\s*)\|[^\]]*\]\]/
    static final String EMPTY = ''
    static final String BLANK = ' '
    static final Pattern NORMALIZE_PATTERN = Pattern.compile("\\p{InCombiningDiacriticalMarks}+")

    List<TuesmonWikiPage> migrateWikiPagesByProject(final RedmineTuesmonRef ref) {
        log.debug("Migrating wiki pages from: '${ref.project.name}'")

        def safeWorkflow =
            executeSafelyAndWarn(
                this.&getCompleteRedmineWikiPageFrom.rcurry(ref) >>
                this.&createTuesmonWikiPage.rcurry(ref) >>
                this.&save)

        return findAllWikiPagesFromProject(ref.redmineIdentifier).findResults(safeWorkflow)
    }

    List<RedmineWikiPage> findAllWikiPagesFromProject(String identifier) {
        return redmineClient.findAllWikiPageByProjectIdentifier(identifier)
    }

    TuesmonWikiPage createTuesmonWikiPage(
        final RedmineWikiPage redmineWikiPage,
        final RedmineTuesmonRef redmineTuesmonRef) {
        log.debug("Building tuesmon wiki page: '${redmineWikiPage.title}'")

        return new TuesmonWikiPage(
            slug: redmineWikiPage.title,
            content: fixContent(redmineWikiPage.text),
            project: redmineTuesmonRef.project,
            attachments: extractWikiAttachments(redmineWikiPage)
        )
    }

    Closure<String> fixContent = this.&fixContentHeaders >> this.&fixContentLinks

    String fixContentHeaders(String content) {
        return (1..5).inject(content) { acc , n -> acc.replaceAll("h${n}.", ("#" * n)) }
    }

    String fixContentLinks(String content) {
        return content
            .replaceAll(REGEX1, regexSustitution)
            .replaceAll(REGEX2, regexSustitution)
    }

    Closure<String> regexSustitution = { Object[] it ->
        def all = it[0]
        def group1 = it[1]
        // replacing spaces in $1
        def sustitution =
            // Tuesmon changes accents
            normalize(
                group1
                    // forcing underscore
                    .replaceAll(BLANK,"_")
                    // Tuesmon converts slugs to lowercase
                    .toLowerCase())
        // replacing grupo 1 with group 0 replacement
        return all.replace(group1, sustitution)
    }

    String normalize(String source) {
        String normalizedString =
            Normalizer.normalize(source, Normalizer.Form.NFD)

        return NORMALIZE_PATTERN
            .matcher(normalizedString)
            .replaceAll(EMPTY)
    }

    List<TuesmonAttachment> extractWikiAttachments(final RedmineWikiPage wiki) {
        log.debug("Adding ${wiki.attachments?.size()} attachments to wiki ${wiki.title}")

        def attachments =
            wiki
                .attachments
                .collect(executeSafelyAndWarn(this.&convertToTuesmonAttachment))
                .findAll { it } // TODO replace with findResults

        return attachments
    }

    RedmineWikiPageDetail getCompleteRedmineWikiPageFrom(
        final RedmineWikiPage redmineWikiPage,
        final RedmineTuesmonRef redmineTuesmonRef) {
        log.debug("Getting complete redmine wiki page: '${redmineWikiPage.title}'")

        return redmineClient
            .findCompleteWikiPageByProjectIdentifierAndTitle(
            redmineTuesmonRef.redmineIdentifier,
            redmineWikiPage.title
        )
    }

    @Override
    TuesmonWikiPage save(TuesmonWikiPage wikipage) {
        log.debug("Trying to save wiki page: ${wikipage.slug}")

        return tuesmonClient.createWiki(wikipage)
    }

    TuesmonWikiPage setWikiHomePage(final List<TuesmonWikiPage> alreadySavedWikiPages) {
        log.debug("Looking for wiki home")

        TuesmonWikiPage home =
            alreadySavedWikiPages.find(wikiPageWithHomeName) ?:
                saveAlternativeWikiHome(alreadySavedWikiPages)

        return home
    }

    Closure<Boolean> wikiPageWithHomeName = { it.slug.toLowerCase() == 'home' }

    TuesmonWikiPage saveAlternativeWikiHome(final List<TuesmonWikiPage> alreadySavedWikiPages) {
        log.debug("No wiki home found in [${alreadySavedWikiPages?.size()}] pages...looking for an alternative")

        TuesmonWikiPage alternative =
            alreadySavedWikiPages.find(byWikiTitle) ?:
                alreadySavedWikiPages.sort(byOldest).first()

        log.debug("Wiki alternative home found: ['${alternative.slug}']")

        return save(
            new TuesmonWikiPage(
                slug: 'home',
                content: fixContent(alternative.content),
                project: alternative.project,
                owner: alternative.owner,
                createdDate: alternative.createdDate,
                attachments: alternative.attachments
            )
        )
    }

    Closure<Boolean> byWikiTitle = filteringBySlugToLowerCase('wiki')
    Closure<Boolean> byOldest = inAscendingOrderBy('createdDate')

    Closure<Boolean> filteringBySlugToLowerCase(String title) {
        return { it.slug.toLowerCase() == title }
    }

    Closure<Boolean> inAscendingOrderBy(String field) {
        return { it."$field" }
    }

}
