package gui.settings

import groovy.transform.Immutable

@Immutable
class Settings {

    String redmineUrl
    String redmineApiKey
    Integer redmineTimeout

    String tuesmonUrl
    String tuesmonUsername
    String tuesmonPassword

}
