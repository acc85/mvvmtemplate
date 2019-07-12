package com.rayt.plugin.mvvmtemplate.actions.actions

import com.android.tools.idea.gradle.npw.project.GradleAndroidModuleTemplate
import com.android.tools.idea.npw.model.ProjectSyncInvoker
import com.android.tools.idea.npw.model.RenderTemplateModel
import com.android.tools.idea.npw.project.AndroidPackageUtils
import com.android.tools.idea.npw.template.ConfigureTemplateParametersStep
import com.android.tools.idea.npw.template.TemplateHandle
import com.android.tools.idea.projectsystem.NamedModuleTemplate
import com.android.tools.idea.ui.wizard.StudioWizardDialogBuilder
import com.android.tools.idea.wizard.model.ModelWizard
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.psi.JavaDirectoryService
import com.intellij.psi.PsiManager
import com.rayt.plugin.mvvmtemplate.actions.utils.Utils
import org.jetbrains.android.facet.AndroidFacet
import java.io.File

class TemplateActivityAction : AnAction() {

    private val TAG = TemplateActivityAction::class.java.simpleName

    private val KEY_R_ANDROID = "r_android"

    private var myCurrentProject: Project? = null
    private var myDataContext: DataContext? = null
    private var myTemplateModel: RenderTemplateModel? = null
    private var myAndroidFacet: AndroidFacet? = null


    override fun actionPerformed(e: AnActionEvent) {
        myCurrentProject = e.getData(DataKeys.PROJECT)
        myDataContext = e.dataContext

        val templateDirectory = Utils.templateDirectory
            ?: // :adam: TODO: 2017-03-01 12:17 - notify the user when something goes wrong
//            NotifyUtil.notifyError(myCurrentProject, "Plugin Error", "Could not load the template.");
            return

//        val targetFile = CommonDataKeys.VIRTUAL_FILE.getData(myDataContext!!)!!
//        var targetDirectory = targetFile
//        if (!targetDirectory.isDirectory) {
//            targetDirectory = targetFile.parent
//            assert(targetDirectory != null)
//        }

        val module = ModuleManager.getInstance(myCurrentProject!!).modules[1]
        myAndroidFacet = AndroidFacet.getInstance(module)
        assert(myAndroidFacet != null)

        val targetFile = ModuleRootManager.getInstance(module).getContentRoots()

//        val modulePath = targetFile[0].parent.createChildDirectory(null,"module").canonicalPath
        var targetDirectory = targetFile[0]
        val activityDescription = e.presentation.text // e.g. "Blank Activity", "Tabbed Activity"

//        val ass =
//            NamedModuleTemplate("", AndroidPackageUtils.getModuleTemplates(myAndroidFacet!!, targetDirectory)[0].paths)

        val ass = GradleAndroidModuleTemplate.createDefaultTemplateAt(File(targetFile[0].canonicalPath))

        val androidSourceSets = AndroidPackageUtils.getModuleTemplates(myAndroidFacet!!, targetDirectory)


        //GET PACKAGE NAME
        val view = LangDataKeys.IDE_VIEW.getData(e.dataContext)
        val imHere = view!!.directories[0].virtualFile
        val psiDirectory = PsiManager.getInstance(myCurrentProject!!).findDirectory(imHere)
        val psiPackage = JavaDirectoryService.getInstance().getPackage(psiDirectory!!)
        val packageName = psiPackage!!.qualifiedName
        myTemplateModel = RenderTemplateModel(
            myAndroidFacet!!, TemplateHandle(templateDirectory), packageName, ass, "",
            object : ProjectSyncInvoker {
                override fun syncProject(p0: Project) {

                }
            }, false
        )

        //List<SourceProvider> sourceProviders = AndroidProjectPaths.getSourceProviders(myAndroidFacet, targetDirectory);
        //String initialPackageSuggestion = AndroidPackageUtils.getPackageForPath(myAndroidFacet, androidSourceSets, targetDirectory);

        val wizardBuilder = ModelWizard.Builder()
        val step = ConfigureTemplateParametersStep(
            myTemplateModel!!,
            "Configure Activity",
            androidSourceSets
        )
        wizardBuilder.addStep(step)
        val modelWizard = wizardBuilder.build()

        // applied at the end of the process
        modelWizard.addResultListener(MyResultListener())

        // create and show the dialog
        StudioWizardDialogBuilder(modelWizard, "New Android Activity")
            .setProject(module.project)
            .build()
            .show()







//        ModuleManager.getInstance(myCurrentProject!!).modules.forEach {module->
//            AndroidFacet.getInstance(module)?.let {facet->
//                myAndroidFacet = AndroidFacet.getInstance(module)
//                val targetFile = ModuleRootManager.getInstance(module).getContentRoots()
//                var targetDirectory = targetFile[0]
//                val ass = GradleAndroidModuleTemplate.createDefaultTemplateAt(File(targetFile[0].canonicalPath))
//                val androidSourceSets = AndroidPackageUtils.getModuleTemplates(myAndroidFacet!!, targetDirectory)
//                val packageName = AndroidPackageUtils.getPackageForApplication(myAndroidFacet!!)
//                myTemplateModel = RenderTemplateModel(
//                    myAndroidFacet!!, TemplateHandle(templateDirectory), packageName, ass, "",
//                    object : ProjectSyncInvoker {
//                        override fun syncProject(p0: Project) {
//
//                        }
//                    }, false
//                )
//                val wizardBuilder = ModelWizard.Builder()
//                val step = ConfigureTemplateParametersStep(
//                    myTemplateModel!!,
//                    "Configure Activity",
//                    androidSourceSets
//                )
//                wizardBuilder.addStep(step)
//                val modelWizard = wizardBuilder.build()
//                // applied at the end of the process
//                modelWizard.addResultListener(MyResultListener())
//                // create and show the dialog
//                StudioWizardDialogBuilder(modelWizard, "New Android Activity")
//                    .setProject(module.project)
//                    .build()
//                    .show()
//            }
//        }
    }


    /**
     * This one dictates the action applied to the generated files after the wizard has finished.
     * Here we move the two method from the fragment file to the modules and component files in injection directory.
     */
    private inner class MyResultListener : ModelWizard.WizardListener {

        fun onWizardFinished(result: Boolean) {
            // do something after wizard has finished
        }
    }
}