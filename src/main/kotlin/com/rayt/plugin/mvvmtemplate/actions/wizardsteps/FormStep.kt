package com.rayt.plugin.mvvmtemplate.actions.wizardsteps

import com.android.tools.idea.wizard.model.ModelWizardStep


abstract class FormStep constructor(open var myModel: DroidWizardModel, title: String) :
    ModelWizardStep<DroidWizardModel>(myModel, title) {

    fun getIntegerValue(s: String): Int? {
        var result: Int?
        try {
            result = Integer.valueOf(s)
        } catch (e: NumberFormatException) {
            // :adam: TODO: 2017-03-06 11:03 - log the error
            result = 0
        }

        return result
    }
}
