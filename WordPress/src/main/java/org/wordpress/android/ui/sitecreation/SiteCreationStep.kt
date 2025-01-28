package org.wordpress.android.ui.sitecreation

import org.wordpress.android.ui.sitecreation.SiteCreationStep.DOMAINS
import org.wordpress.android.ui.sitecreation.SiteCreationStep.INTENTS
import org.wordpress.android.ui.sitecreation.SiteCreationStep.PLANS
import org.wordpress.android.ui.sitecreation.SiteCreationStep.PROGRESS
import org.wordpress.android.ui.sitecreation.SiteCreationStep.SITE_DESIGNS
import org.wordpress.android.ui.sitecreation.SiteCreationStep.SITE_NAME
import org.wordpress.android.ui.sitecreation.SiteCreationStep.SITE_PREVIEW
import org.wordpress.android.util.config.PlansInSiteCreationFeatureConfig
import org.wordpress.android.util.config.SiteNameFeatureConfig
import org.wordpress.android.util.wizard.WizardStep
import javax.inject.Inject
import javax.inject.Singleton

enum class SiteCreationStep : WizardStep {
    SITE_DESIGNS, DOMAINS, PLANS, PROGRESS, SITE_PREVIEW, INTENTS, SITE_NAME;
}

@Singleton
class SiteCreationStepsProvider @Inject constructor(
    private val siteNameFeatureConfig: SiteNameFeatureConfig,
    private val plansInSiteCreationFeatureConfig: PlansInSiteCreationFeatureConfig
) {
    private val isSiteNameEnabled get() = siteNameFeatureConfig.isEnabled()
    private val isPlansEnabled get() = plansInSiteCreationFeatureConfig.isEnabled()

    fun getSteps(): List<SiteCreationStep> = when {
        isPlansEnabled -> listOf(INTENTS, SITE_DESIGNS, DOMAINS, PLANS, PROGRESS, SITE_PREVIEW)
        isSiteNameEnabled -> listOf(INTENTS, SITE_NAME, SITE_DESIGNS, PROGRESS, SITE_PREVIEW)
        else -> listOf(INTENTS, SITE_DESIGNS, DOMAINS, PROGRESS, SITE_PREVIEW)
    }
}
