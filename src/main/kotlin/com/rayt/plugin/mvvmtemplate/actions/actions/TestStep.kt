/*
 * Copyright (C) 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.tools.idea.npw.project

import com.android.tools.idea.templates.Template.CATEGORY_APPLICATION
import com.google.common.collect.Lists.newArrayList
import java.util.stream.Collectors.toList
import java.util.stream.Collectors.toMap
import org.jetbrains.android.util.AndroidBundle.message

import com.android.tools.adtui.ASGallery
import com.android.tools.adtui.stdui.CommonTabbedPane
import com.android.tools.adtui.util.FormScalingUtil
import com.android.tools.idea.flags.StudioFlags
import com.android.tools.idea.npw.FormFactor
import com.android.tools.idea.npw.cpp.ConfigureCppSupportStep
import com.android.tools.idea.npw.model.NewProjectModel
import com.android.tools.idea.npw.model.NewProjectModuleModel
import com.android.tools.idea.npw.model.RenderTemplateModel
import com.android.tools.idea.npw.template.ConfigureTemplateParametersStep
import com.android.tools.idea.npw.template.TemplateHandle
import com.android.tools.idea.npw.ui.ActivityGallery
import com.android.tools.idea.npw.ui.WizardGallery
import com.android.tools.idea.templates.TemplateManager
import com.android.tools.idea.templates.TemplateMetadata
import com.android.tools.idea.wizard.model.ModelWizard
import com.android.tools.idea.wizard.model.ModelWizardStep
import com.google.common.collect.Lists
import com.google.common.collect.Maps
import com.intellij.ui.components.JBList
import java.awt.Image
import java.awt.event.ActionEvent
import java.io.File
import java.util.ArrayList
import java.util.Arrays
import java.util.Comparator
import java.util.Objects
import javax.swing.AbstractAction
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.event.ListSelectionListener
import org.jetbrains.android.sdk.AndroidSdkUtils
import org.jetbrains.android.util.AndroidBundle
import javax.annotation.Nullable
import javax.swing.event.ListSelectionEvent

/**
 * First page in the New Project wizard that allows user to select the Form Factor (Mobile, Wear, TV, etc) and its
 * Template ("Empty Activity", "Basic", "Nav Drawer", etc)
 * TODO: "No Activity" needs a Template Icon place holder
 */
class ChooseAndroidProjectStep(model: NewProjectModel) : ModelWizardStep<NewProjectModel>(model, message("android.wizard.project.new.choose")) {
    // To have the sequence specified by design, we hardcode the sequence.
    private val ORDERED_ACTIVITY_NAMES = arrayOf(
        "Basic Activity",
        "Empty Activity",
        "Bottom Navigation Activity",
        "Fullscreen Activity",
        "Master/Detail Flow",
        "Navigation Drawer Activity",
        "Google Maps Activity",
        "Login Activity",
        "Scrolling Activity",
        "Tabbed Activity"
    )

    private val myFormFactors = ArrayList<FormFactorInfo>()

    lateinit var myRootPanel: JPanel
    lateinit var myTabsPanel: CommonTabbedPane
    lateinit var myNewProjectModuleModel: NewProjectModuleModel

    override fun createDependentSteps(): Collection<ModelWizardStep<*>> {
        this.myNewProjectModuleModel = NewProjectModuleModel(this.model)
        val renderModel = myNewProjectModuleModel.getExtraRenderTemplateModel()
        return Lists.newArrayList(
            *arrayOf(
                ConfigureAndroidProjectStep(this.myNewProjectModuleModel, this.model),
                ConfigureCppSupportStep(
                    this.model
                ),
                ConfigureTemplateParametersStep(
                    renderModel,
                    AndroidBundle.message("android.wizard.config.activity.title", *arrayOfNulls(0)),
                    Lists.newArrayList()
                )
            )
        )
    }


    override fun onWizardStarting(wizard: ModelWizard.Facade) {
        populateFormFactors()

        for (formFactorInfo in myFormFactors) {
            val tabPanel = formFactorInfo.tabPanel
            myTabsPanel!!.addTab(formFactorInfo.formFactor.toString(), tabPanel.myRootPanel)

            tabPanel.myGallery.setDefaultAction(object : AbstractAction() {
                override fun actionPerformed(actionEvent: ActionEvent) {
                    wizard.goForward()
                }
            })


            val activitySelectedListener = object: ListSelectionListener{
                override fun valueChanged(e: ListSelectionEvent?) {
                    val selectedTemplate = tabPanel.myGallery.selectedElement
                    if (selectedTemplate != null) {
                        tabPanel.myTemplateName.text = selectedTemplate.getImageLabel()
                        tabPanel.myTemplateDesc.text = "<html>" + selectedTemplate.getTemplateDescription() + "</html>"
                        tabPanel.myDocumentationLink.isVisible = selectedTemplate.myIsCppTemplate
                    }
                }
            }
//            val activitySelectedListener  = { selectionEvent ->
//                val selectedTemplate = tabPanel.myGallery.selectedElement
//                if (selectedTemplate != null) {
//                    tabPanel.myTemplateName.text = selectedTemplate.imageLabel
//                    tabPanel.myTemplateDesc.text = "<html>" + selectedTemplate.templateDescription + "</html>"
//                    tabPanel.myDocumentationLink.isVisible = selectedTemplate.isCppTemplate
//                }
//            }

            tabPanel.myGallery.addListSelectionListener(activitySelectedListener)
            activitySelectedListener.valueChanged(null)
        }

        FormScalingUtil.scaleComponentTree(this.javaClass, myRootPanel)
    }

    override fun onProceeding() {
        val formFactorInfo = myFormFactors[myTabsPanel!!.selectedIndex]
        val selectedTemplate = formFactorInfo.tabPanel.myGallery.selectedElement

        model.enableCppSupport().set(selectedTemplate!!.myIsCppTemplate)
        myNewProjectModuleModel!!.formFactor().set(formFactorInfo.formFactor)
        myNewProjectModuleModel!!.moduleTemplateFile().setNullableValue(formFactorInfo.templateFile)
        myNewProjectModuleModel!!.renderTemplateHandle().setNullableValue(selectedTemplate.myTemplate)

        val extraStepTemplateHandle =
            if (formFactorInfo.formFactor == FormFactor.THINGS) selectedTemplate.myTemplate else null
        myNewProjectModuleModel!!.extraRenderTemplateModel.templateHandle = extraStepTemplateHandle
    }

    override fun getComponent(): JComponent {
        return myRootPanel
    }

    override fun getPreferredFocusComponent(): JComponent {
        return myTabsPanel
    }

    private fun populateFormFactors() {
        val formFactorInfoMap = Maps.newTreeMap<FormFactor, FormFactorInfo>()
        val manager = TemplateManager.getInstance()
        val applicationTemplates = manager.getTemplatesInCategory(CATEGORY_APPLICATION)

        for (templateFile in applicationTemplates) {
            val metadata = manager.getTemplateMetadata(templateFile)
            if (metadata == null || metadata.formFactor == null) {
                continue
            }
            val formFactor = FormFactor.get(metadata.formFactor!!)
            if (formFactor == FormFactor.GLASS && !AndroidSdkUtils.isGlassInstalled()) {
                // Only show Glass if you've already installed the SDK
                continue
            }
            val prevFormFactorInfo = formFactorInfoMap[formFactor]
            val templateMinSdk = metadata.minSdk

            if (prevFormFactorInfo == null) {
                val minSdk = Math.max(templateMinSdk, formFactor.minOfflineApiLevel)
                val tabPanel = ChooseAndroidProjectPanel(createGallery(title, formFactor))
                formFactorInfoMap[formFactor] = FormFactorInfo(templateFile, formFactor, minSdk, tabPanel)
            } else if (templateMinSdk > prevFormFactorInfo.minSdk) {
                prevFormFactorInfo.minSdk = templateMinSdk
                prevFormFactorInfo.templateFile = templateFile
            }
        }

        myFormFactors.addAll(formFactorInfoMap.values)
        myFormFactors.sortWith(Comparator.comparing { f -> f.formFactor })
    }

    private fun getFilteredTemplateHandles(formFactor: FormFactor): List<TemplateHandle> {
        val templateHandles = TemplateManager.getInstance().getTemplateList(formFactor)


        
//        if (formFactor == FormFactor.MOBILE) {
//            val entryMap = templateHandles.stream()
//                .collect<Map<String, TemplateHandle>, Any>(toMap({ it -> it.metadata.title }, { it -> it }))
//            return Arrays.stream(ORDERED_ACTIVITY_NAMES).map { it -> entryMap[it] }
//                .filter(Predicate<TemplateHandle> { Objects.nonNull(it) }).collect<List<TemplateHandle>, Any>(toList())
//        }

        return templateHandles
    }

    private fun createGallery(title: String, formFactor: FormFactor): ASGallery<TemplateRenderer> {
        val templateHandles = getFilteredTemplateHandles(formFactor)

        val templateRenderers = Lists.newArrayListWithExpectedSize<TemplateRenderer>(templateHandles.size + 2)
        templateRenderers.add(TemplateRenderer(null, false)) // "Add No Activity" entry
        for (templateHandle in templateHandles) {
            templateRenderers.add(TemplateRenderer(templateHandle, false))
        }

        if (formFactor == FormFactor.MOBILE) {
            templateRenderers.add(TemplateRenderer(null, true)) // "Native C++" entry
        }

        val listItems = templateRenderers.toTypedArray()

        val gallery = WizardGallery<TemplateRenderer>(title, ActivityGallery.getTemplateImage(null, false),TemplateRenderer::getImageLabel)
        gallery.model = JBList.createDefaultListModel(*listItems as Array<Any>)
        gallery.selectedIndex = getDefaultSelectedTemplateIndex(listItems)

        return gallery
    }

    private fun getDefaultSelectedTemplateIndex(templateRenderers: Array<TemplateRenderer>): Int {
        for (i in templateRenderers.indices) {
            if (templateRenderers[i].getImageLabel() == "Empty Activity") {
                return i
            }
        }

        // Default template not found. Instead, return the index to the first valid template renderer (e.g. skip "Add No Activity", etc.)
        for (i in templateRenderers.indices) {
            if (templateRenderers[i].myTemplate != null) {
                return i
            }
        }

        assert(false) { "No valid Template found" }
        return 0
    }

    private class FormFactorInfo internal constructor(
        internal var templateFile: File, internal val formFactor: FormFactor, internal var minSdk: Int,
        internal val tabPanel: ChooseAndroidProjectPanel<TemplateRenderer>
    )


    class TemplateRenderer(val myTemplate:TemplateHandle?,  val myIsCppTemplate:Boolean){

        fun getImageLabel(): String {
            return ActivityGallery.getTemplateImageLabel(myTemplate, myIsCppTemplate)
        }

        fun getTemplateDescription(): String {
            return ActivityGallery.getTemplateDescription(myTemplate, myIsCppTemplate)
        }

        override fun toString(): String {
            return getImageLabel()
        }

        fun getImage(): Image? {
            return ActivityGallery.getTemplateImage(myTemplate, myIsCppTemplate)
        }

        companion object{
            fun getImage(): Function<Image?> {
                var t:TemplateRenderer = TemplateRenderer(null,false)
                return ActivityGallery.getTemplateImage(t.myTemplate, t.myIsCppTemplate)
            }
        }

    }
}

//    private class TemplateRenderer internal constructor(
//        @param:Nullable @field:Nullable @get:Nullable
//        internal val template: TemplateHandle, internal val isCppTemplate: Boolean
//    ) {
//
//        internal val imageLabel: String
//            @NotNull
//            get() = ActivityGallery.getTemplateImageLabel(template, isCppTemplate)
//
//        internal val templateDescription: String
//            @NotNull
//            get() = ActivityGallery.getTemplateDescription(template, isCppTemplate)
//
//        /**
//         * Return the image associated with the current template, if it specifies one, or null otherwise.
//         */
//        internal val image: Image?
//            @Nullable
//            get() = ActivityGallery.getTemplateImage(template, isCppTemplate)
//
//        @NotNull
//        override fun toString(): String {
//            return imageLabel
//        }
//}