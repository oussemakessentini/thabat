import { createNativeStackNavigator } from "@react-navigation/native-stack";
import React from "react";

import {
    AppBootstrapLoading,
    useAppReadyState,
} from "../AppInitializer";
import { LoginScreen } from "../features/auth/screens/LoginScreen";
import { RegisterScreen } from "../features/auth/screens/RegisterScreen";
import { HomeScreen } from "../features/home/screens/HomeScreen";
import { NotificationsScreen } from "../features/notifications/screens/NotificationsScreen";
import { GoalsOnboardingScreen } from "../features/onboarding/screens/GoalsOnboardingScreen";
import { LevelsOnboardingScreen } from "../features/onboarding/screens/LevelsOnboardingScreen";
import { PreferencesOnboardingScreen } from "../features/onboarding/screens/PreferencesOnboardingScreen";
import { WelcomeOnboardingScreen } from "../features/onboarding/screens/WelcomeOnboardingScreen";
import { OnboardingProvider } from "../features/onboarding/state/OnboardingContext";
import { PrayerAssessmentResultScreen } from "../features/prayer/screens/PrayerAssessmentResultScreen";
import { PrayerAssessmentScreen } from "../features/prayer/screens/PrayerAssessmentScreen";
import { PrayerProgressScreen } from "../features/prayer/screens/PrayerProgressScreen";
import { RecoveryHistoryScreen } from "../features/prayer/screens/RecoveryHistoryScreen";
import { QuranDailyGoalScreen } from "../features/quran/screens/QuranDailyGoalScreen";
import { QuranPageDetailScreen } from "../features/quran/screens/QuranPageDetailScreen";
import { QuranSectionDetailScreen } from "../features/quran/screens/QuranSectionDetailScreen";
import { QuranTodayScreen } from "../features/quran/screens/QuranTodayScreen";
import { QuranTrackerScreen } from "../features/quran/screens/QuranTrackerScreen";
import { SplashScreen } from "../features/splash/screens/SplashScreen";

import type {
    AppStackParamList,
    AuthStackParamList,
    OnboardingStackParamList,
} from "./navigation.types";

const AuthStack = createNativeStackNavigator<AuthStackParamList>();
const OnboardingStack = createNativeStackNavigator<OnboardingStackParamList>();
const AppStack = createNativeStackNavigator<AppStackParamList>();

function AuthNavigator(): React.JSX.Element {
    return (
        <AuthStack.Navigator
            id="AuthStack"
            initialRouteName="Splash"
            screenOptions={{ headerShown: false }}
        >
            <AuthStack.Screen name="Splash" component={SplashScreen} />
            <AuthStack.Screen name="Login" component={LoginScreen} />
            <AuthStack.Screen name="Register" component={RegisterScreen} />
        </AuthStack.Navigator>
    );
}

function OnboardingNavigator(): React.JSX.Element {
    return (
        <OnboardingProvider>
            <OnboardingStack.Navigator
                id="OnboardingStack"
                initialRouteName="WelcomeOnboarding"
                screenOptions={{ headerShown: false }}
            >
                <OnboardingStack.Screen
                    name="WelcomeOnboarding"
                    component={WelcomeOnboardingScreen}
                />
                <OnboardingStack.Screen
                    name="GoalsOnboarding"
                    component={GoalsOnboardingScreen}
                />
                <OnboardingStack.Screen
                    name="LevelsOnboarding"
                    component={LevelsOnboardingScreen}
                />
                <OnboardingStack.Screen
                    name="PreferencesOnboarding"
                    component={PreferencesOnboardingScreen}
                />
            </OnboardingStack.Navigator>
        </OnboardingProvider>
    );
}

function AppNavigatorStack(): React.JSX.Element {
    return (
        <AppStack.Navigator
            id="AppStack"
            initialRouteName="Home"
            screenOptions={{ headerShown: false }}
        >
            <AppStack.Screen name="Home" component={HomeScreen} />
            <AppStack.Screen
                name="Notifications"
                component={NotificationsScreen}
            />
            <AppStack.Screen
                name="PrayerAssessment"
                component={PrayerAssessmentScreen}
            />
            <AppStack.Screen
                name="PrayerAssessmentResult"
                component={PrayerAssessmentResultScreen}
            />
            <AppStack.Screen
                name="PrayerProgress"
                component={PrayerProgressScreen}
            />
            <AppStack.Screen
                name="RecoveryHistory"
                component={RecoveryHistoryScreen}
            />
            <AppStack.Screen
                name="QuranTracker"
                component={QuranTrackerScreen}
            />
            <AppStack.Screen
                name="QuranDailyGoal"
                component={QuranDailyGoalScreen}
            />
            <AppStack.Screen
                name="QuranToday"
                component={QuranTodayScreen}
            />
            <AppStack.Screen
                name="QuranPageDetail"
                component={QuranPageDetailScreen}
            />
            <AppStack.Screen
                name="QuranSectionDetail"
                component={QuranSectionDetailScreen}
            />
        </AppStack.Navigator>
    );
}

export function AppNavigator(): React.JSX.Element {
    const ready = useAppReadyState();

    if (ready.status === "loading") {
        return <AppBootstrapLoading />;
    }

    if (ready.status === "unauthenticated") {
        return <AuthNavigator />;
    }

    if (ready.status === "onboarding") {
        return <OnboardingNavigator />;
    }

    return <AppNavigatorStack />;
}
