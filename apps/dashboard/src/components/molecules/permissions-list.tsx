interface PermissionsListProps {
    permissions: string[];
    heading?: string;
}

export function PermissionsList({
    permissions,
    heading = "Permissions",
}: Readonly<PermissionsListProps>) {
    return (
        <section>
            <h3 className="text-foreground text-sm font-semibold uppercase tracking-wider opacity-80 mb-4">
                {heading}
            </h3>

            <div className="rounded-lg border border-border bg-background p-4 space-y-3">
                {permissions.length === 0 ? (
                    <p className="text-sm text-muted-foreground/70">
                        Select a service to view the permissions
                    </p>
                ) : (
                    permissions.map((permission) => (
                        <div
                            key={permission}
                            className="rounded-md bg-card px-4 py-3 text-sm text-foreground"
                        >
                            - {permission}
                        </div>
                    ))
                )}
            </div>
        </section>
    );
}
