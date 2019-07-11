package com.rayt.plugin.mvvmtemplate.actions.actions

import com.android.tools.idea.ui.wizard.StudioWizardDialogBuilder
import com.android.tools.idea.wizard.model.ModelWizard
import com.intellij.ide.actions.NewProjectAction
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.rayt.plugin.mvvmtemplate.actions.wizardsteps.DroidWizardModel
import com.rayt.plugin.mvvmtemplate.actions.wizardsteps.FormStepA
import com.rayt.plugin.mvvmtemplate.actions.wizardsteps.FormStepB


class WizardAction : AnAction() {

    override fun actionPerformed(anActionEvent: AnActionEvent) {
        val project = anActionEvent.getData(PlatformDataKeys.PROJECT)

        val wizardModel = DroidWizardModel(project!!)
        val stepA = FormStepA(wizardModel)
        val stepB = FormStepB(wizardModel)
        val wizard = ModelWizard.Builder()
            .addStep(stepA)
            .addStep(stepB)
            .build()
        StudioWizardDialogBuilder(wizard, "Create New Droidcon Project").build().show()
    }

    companion object {

        private val TAG = NewProjectAction::class.java.simpleName
    }

}
