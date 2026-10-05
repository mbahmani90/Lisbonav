import FirebaseAnalytics
import Shared

/// `Analytics` backed by Firebase. Collection starts off (Info.plist) until consent.
/// Both modules have an `Analytics` type, so each is qualified.
final class FirebaseIosAnalytics: Shared.Analytics {

    func log(event: AnalyticsEvent) {
        // Kotlin Long / Double arrive as KotlinLong / KotlinDouble (NSNumber), which Firebase accepts.
        FirebaseAnalytics.Analytics.logEvent(event.name, parameters: event.params)
    }

    func setCollectionEnabled(enabled: Bool) {
        FirebaseAnalytics.Analytics.setAnalyticsCollectionEnabled(enabled)
    }
}
