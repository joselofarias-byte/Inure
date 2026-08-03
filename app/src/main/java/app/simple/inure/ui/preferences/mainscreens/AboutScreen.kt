package app.simple.inure.ui.preferences.mainscreens

import android.os.Bundle
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.text.toSpannable
import app.simple.inure.BuildConfig
import app.simple.inure.R
import app.simple.inure.decorations.ripple.DynamicRippleLinearLayout
import app.simple.inure.decorations.ripple.DynamicRippleRelativeLayout
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.extensions.fragments.ScopedFragment
import app.simple.inure.preferences.TrialPreferences
import app.simple.inure.themes.manager.ThemeManager
import app.simple.inure.util.AppUtils

class AboutScreen : ScopedFragment() {

    private lateinit var versionTag: TypeFaceTextView
    private lateinit var version: TypeFaceTextView
    private lateinit var changelogs: DynamicRippleRelativeLayout
    private lateinit var userAgreement: DynamicRippleRelativeLayout
    private lateinit var credits: DynamicRippleRelativeLayout
    private lateinit var licenses: DynamicRippleRelativeLayout
    private lateinit var privacyPolicy: DynamicRippleLinearLayout

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.preferences_about, container, false)

        versionTag = view.findViewById(R.id.app_version_tag)
        version = view.findViewById(R.id.app_version_value)
        changelogs = view.findViewById(R.id.changelogs)
        userAgreement = view.findViewById(R.id.user_agreement)
        credits = view.findViewById(R.id.credits)
        licenses = view.findViewById(R.id.licenses)
        privacyPolicy = view.findViewById(R.id.toc)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startPostponedEnterTransition()
        setAppVersionTag()

        if (BuildConfig.JOSELOFARIAS_UNLOCKED) {
            version.append("-joselofarias")
        } else if (TrialPreferences.isFullVersion()) {
            if (TrialPreferences.isUnlockerVerificationRequired()) {
                version.append("-full_unlckr")
            } else {
                version.append("-full_grdlkey")
            }
        } else {
            version.append("-trial (${TrialPreferences.getDaysLeft()} days left)")
        }

        credits.setOnClickListener {
            openWebPage(getString(R.string.credits))
        }

        changelogs.setOnClickListener {
            openWebPage(getString(R.string.change_logs))
        }

        licenses.setOnClickListener {
            openWebPage(getString(R.string.open_source_licenses))
        }

        userAgreement.setOnClickListener {
            openWebPage(getString(R.string.user_agreements))
        }

        privacyPolicy.setOnClickListener {
            openWebPage(getString(R.string.privacy_policy))
        }
    }

    private fun setAppVersionTag() {
        when {
            AppUtils.isJoseloFariasFlavor() -> {
                versionTag.append(" (JoseloFarias Fork/FOSS)")
                ForegroundColorSpan(ThemeManager.theme.textViewTheme.tertiaryTextColor).let { foregroundColorSpan ->
                    versionTag.text.toSpannable().let { spannable ->
                        spannable.setSpan(foregroundColorSpan, versionTag.text.indexOfFirst { it == '(' },
                                          versionTag.text.length,
                                          Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                        versionTag.text = spannable
                    }
                }
            }
            AppUtils.isPlayFlavor() -> {
                versionTag.append(" (Play Store)")
                ForegroundColorSpan(ThemeManager.theme.textViewTheme.tertiaryTextColor).let { foregroundColorSpan ->
                    versionTag.text.toSpannable().let { spannable ->
                        spannable.setSpan(foregroundColorSpan, versionTag.text.indexOfFirst { it == '(' },
                                          versionTag.text.length,
                                          Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                        versionTag.text = spannable
                    }
                }
            }
            AppUtils.isBetaFlavor() -> {
                versionTag.append(" (Beta Testing)")
                ForegroundColorSpan(ThemeManager.theme.textViewTheme.tertiaryTextColor).let { foregroundColorSpan ->
                    versionTag.text.toSpannable().let { spannable ->
                        spannable.setSpan(foregroundColorSpan, versionTag.text.indexOfFirst { it == '(' },
                                          versionTag.text.length,
                                          Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                        versionTag.text = spannable
                    }
                }
            }
        }
    }

    companion object {
        fun newInstance(): AboutScreen {
            val args = Bundle()
            val fragment = AboutScreen()
            fragment.arguments = args
            return fragment
        }

        const val TAG = "AboutScreen"
    }
}
