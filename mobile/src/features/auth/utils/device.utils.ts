export function resolveDeviceTimezone(): string {
    try {
        const timezone = Intl.DateTimeFormat().resolvedOptions().timeZone;

        if (typeof timezone === "string" && timezone.trim().length > 0) {
            return timezone;
        }
    } catch {
        // Fall through to default.
    }

    return "America/Chicago";
}
