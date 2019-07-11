package com.rayt.plugin.mvvmtemplate.actions.wizardsteps

import com.android.tools.idea.npw.model.NewProjectModel
import com.intellij.openapi.project.Project
import com.rayt.plugin.mvvmtemplate.actions.utils.Utils


class DroidWizardModel(private val myProject: Project) : NewProjectModel() {

    var firstNumber = 0
    var secondNumber = 0

    /**
     * This is where the final step of the wizard is executed.
     */
    public override fun handleFinished() {
        val result = firstNumber.toLong() + secondNumber.toLong()
        Utils.showDialogMessage(myProject, "Result", " A + B = $result")
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //                                             SETTERS
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    companion object {

        private val TAG = DroidWizardModel::class.java.simpleName
    }

}
