import SwiftUI
import Shared

@main
struct iOSApp: App {
    init() {
        // No analytics yet: Firebase replaces NoOpAnalytics in a later step.
        KoinIosKt.doInitKoinIos(analytics: NoOpAnalytics.shared)
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}