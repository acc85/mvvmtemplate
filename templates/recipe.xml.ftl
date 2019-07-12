<?xml version="1.0"?>
<recipe>

    <instantiate from="root/src/app_package/TemplateActivity.java.ftl"
                    to="${escapeXmlAttribute(srcOut)}/${activityName}Activity.java" />

    <instantiate from="root/res/layout/activity_template.xml.ftl"
                    to="${escapeXmlAttribute(resOut)}/layout/activity_${activityName?lower_case}.xml" />

    <merge from="root/res/values/strings.xml.ftl"
        to="${escapeXmlAttribute(resOut)}/values/strings.xml" />

</recipe>
