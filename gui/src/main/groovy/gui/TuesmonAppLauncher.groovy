package gui

import static org.jdesktop.swingx.plaf.UIManagerExt.getSafeColor

import javax.swing.UIManager
import javax.swing.LookAndFeel

import com.jgoodies.looks.plastic.*
import com.jgoodies.looks.plastic.theme.*

import org.viewaframework.core.DefaultApplicationLauncher

import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;

import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.FontUIResource;
import javax.swing.plaf.InsetsUIResource;
import javax.swing.plaf.metal.MetalLookAndFeel;

import javax.swing.InputMap
import javax.swing.KeyStroke

import org.jdesktop.swingx.JXTitledPanel;
import org.jdesktop.swingx.painter.MattePainter;

import org.jdesktop.swingx.plaf.PainterUIResource;

class TuesmonAppLauncher extends DefaultApplicationLauncher {

    LookAndFeel getLookAndFeel() {
        return buildTuesmonLookAndFeel()
    }

    PlasticLookAndFeel buildTuesmonLookAndFeel() {
        def lookAndFeel = new PlasticLookAndFeel()
        lookAndFeel.setCurrentTheme(new ExperienceGreen())

        // MODIFICATIONS OVER SELECTED LnF
        // -------------------------------
        // Modifications should be done once the LnF has been set
        modifyTitlePanelTuesmonBackground()

        return lookAndFeel
    }

    void modifyTitlePanelTuesmonBackground() {
        Color primaryBackground = getSafeColor('ProgressBar.selectionBackground', new ColorUIResource(114, 161, 20))
        Color lighterPrimaryBackground = primaryBackground.brighter()

        GradientPaint gradient = new GradientPaint(0, 0, primaryBackground, 0, 1, lighterPrimaryBackground)

        // Making title panel looks like Tuesmon colors
        UIManager.put(
            'JXTitledPanel.titlePainter',
            new PainterUIResource<JXTitledPanel>(new MattePainter(gradient, true))
        )

        // Change button key action to ENTER
        InputMap im = (InputMap)UIManager.get("Button.focusInputMap")
        im.put( KeyStroke.getKeyStroke( "ENTER" ), "pressed" )
        im.put( KeyStroke.getKeyStroke( "released ENTER" ), "released" )

        UIManager.put('Button.focusInputMap', im)

    }

}
