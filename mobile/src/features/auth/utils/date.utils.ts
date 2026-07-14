export const MIN_AGE = 5;
export const MAX_AGE = 120;

export function calculateAge(dateOfBirth: Date): number {
    const today = new Date();
    let age = today.getFullYear() - dateOfBirth.getFullYear();
    const monthDiff = today.getMonth() - dateOfBirth.getMonth();

    if (
        monthDiff < 0
        || (monthDiff === 0 && today.getDate() < dateOfBirth.getDate())
    ) {
        age -= 1;
    }

    return age;
}

export function isDateInPast(dateOfBirth: Date): boolean {
    const today = new Date();
    today.setHours(0, 0, 0, 0);

    const normalizedDate = new Date(dateOfBirth);
    normalizedDate.setHours(0, 0, 0, 0);

    return normalizedDate < today;
}

export function subtractYears(years: number): Date {
    const date = new Date();
    date.setHours(0, 0, 0, 0);
    date.setFullYear(date.getFullYear() - years);
    return date;
}

export function formatDateForApi(date: Date): string {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, "0");
    const day = String(date.getDate()).padStart(2, "0");

    return `${year}-${month}-${day}`;
}

export function parseApiDate(value: string): Date | undefined {
    if (!value) {
        return undefined;
    }

    const match = /^(\d{4})-(\d{2})-(\d{2})$/.exec(value);

    if (!match) {
        return undefined;
    }

    const year = Number(match[1]);
    const month = Number(match[2]);
    const day = Number(match[3]);
    const date = new Date(year, month - 1, day);

    if (
        date.getFullYear() !== year
        || date.getMonth() !== month - 1
        || date.getDate() !== day
    ) {
        return undefined;
    }

    return date;
}

export function formatDateOfBirthDisplay(value: string): string {
    const date = parseApiDate(value);

    if (!date) {
        return "";
    }

    return new Intl.DateTimeFormat("en-US", {
        year: "numeric",
        month: "long",
        day: "numeric",
    }).format(date);
}
