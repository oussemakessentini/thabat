import React, {
    createContext,
    useCallback,
    useContext,
    useMemo,
    useState,
} from "react";

import type {
    ExperienceMode,
    JourneyGoal,
    OnboardingDraft,
    PrayerLevel,
    QuranLevel,
    ReminderPreference,
} from "../types/onboarding.types";

type OnboardingContextValue = {
    draft: OnboardingDraft;
    toggleGoal: (goal: JourneyGoal) => void;
    setPrayerLevel: (level: PrayerLevel) => void;
    setQuranLevel: (level: QuranLevel) => void;
    setExperienceMode: (mode: ExperienceMode) => void;
    setReminderPreference: (preference: ReminderPreference) => void;
    resetDraft: () => void;
};

const initialDraft: OnboardingDraft = {
    experienceMode: null,
    selectedGoals: [],
    prayerLevel: null,
    quranLevel: null,
    reminderPreference: null,
};

const OnboardingContext = createContext<OnboardingContextValue | undefined>(
    undefined,
);

type OnboardingProviderProps = {
    children: React.ReactNode;
};

export function OnboardingProvider({
    children,
}: OnboardingProviderProps): React.JSX.Element {
    const [draft, setDraft] = useState<OnboardingDraft>(initialDraft);

    const toggleGoal = useCallback((goal: JourneyGoal): void => {
        setDraft((current) => {
            const exists = current.selectedGoals.includes(goal);
            return {
                ...current,
                selectedGoals: exists
                    ? current.selectedGoals.filter((item) => item !== goal)
                    : [...current.selectedGoals, goal],
            };
        });
    }, []);

    const setPrayerLevel = useCallback((prayerLevel: PrayerLevel): void => {
        setDraft((current) => ({ ...current, prayerLevel }));
    }, []);

    const setQuranLevel = useCallback((quranLevel: QuranLevel): void => {
        setDraft((current) => ({ ...current, quranLevel }));
    }, []);

    const setExperienceMode = useCallback(
        (experienceMode: ExperienceMode): void => {
            setDraft((current) => ({ ...current, experienceMode }));
        },
        [],
    );

    const setReminderPreference = useCallback(
        (reminderPreference: ReminderPreference): void => {
            setDraft((current) => ({ ...current, reminderPreference }));
        },
        [],
    );

    const resetDraft = useCallback((): void => {
        setDraft(initialDraft);
    }, []);

    const value = useMemo(
        () => ({
            draft,
            toggleGoal,
            setPrayerLevel,
            setQuranLevel,
            setExperienceMode,
            setReminderPreference,
            resetDraft,
        }),
        [
            draft,
            toggleGoal,
            setPrayerLevel,
            setQuranLevel,
            setExperienceMode,
            setReminderPreference,
            resetDraft,
        ],
    );

    return (
        <OnboardingContext.Provider value={value}>
            {children}
        </OnboardingContext.Provider>
    );
}

export function useOnboardingDraft(): OnboardingContextValue {
    const context = useContext(OnboardingContext);
    if (!context) {
        throw new Error(
            "useOnboardingDraft must be used within OnboardingProvider",
        );
    }
    return context;
}
