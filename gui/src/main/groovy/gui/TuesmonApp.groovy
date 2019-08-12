package gui

import gui.root.RootView

import org.viewaframework.annotation.View
import org.viewaframework.annotation.Views
import org.viewaframework.annotation.ViewsPerspective

import org.viewaframework.core.DefaultApplication
import org.viewaframework.core.ApplicationException
import org.viewaframework.view.perspective.PerspectiveConstraint
import org.viewaframework.docking.mydoggy.MyDoggyPerspective


//import org.viewaframework.integration.spring.SpringApplicationLauncher

@Views([
    @View(type=RootView,isRoot=true),
    @View(type=gui.console.ConsoleView, position=PerspectiveConstraint.BOTTOM)
])
@ViewsPerspective(MyDoggyPerspective)
class TuesmonApp extends DefaultApplication {
    static void main(String[] args) {
        //new SpringApplicationLauncher().execute(TuesmonApp)
        new TuesmonAppLauncher().execute(TuesmonApp)
    }

    @Override
    public void prepare() throws ApplicationException {
        super.prepare()
        // App starts so quickly that it doesn't give any chance
        // to enjoy splash screen :P
        Thread.sleep(4000)
    }

}
