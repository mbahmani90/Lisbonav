import FirebaseCore
import SwiftUI
import Shared

@main
struct iOSApp: App {
    init() {
        // Firebase first: it must be configured before anything logs.
        KoinIosKt.doInitKoinIos(analytics: Self.createAnalytics())
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }

    /// Firebase when GoogleService-Info.plist is bundled (gitignored, so a fresh clone has none),
    /// otherwise nothing is logged.
    private static func createAnalytics() -> Shared.Analytics {
        guard Bundle.main.path(forResource: "GoogleService-Info", ofType: "plist") != nil else {
            return NoOpAnalytics.shared
        }
        FirebaseApp.configure()
        return FirebaseIosAnalytics()
    }
}
