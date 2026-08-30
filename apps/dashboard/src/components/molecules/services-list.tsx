import { Button } from "../atoms/button";

export interface ServiceOption {
    id: string;
    name: string;
}

interface ServicesListProps {
    servicesAvailable: ServiceOption[];
    selectedServices: string[];
    onServiceToggle: (serviceId: string) => void;
    onSelectAll: () => void;
    heading?: string;
    className?: string;
}

export function ServicesList({
    servicesAvailable,
    selectedServices,
    onServiceToggle,
    onSelectAll,
    heading = "Services we offer",
    className,
}: Readonly<ServicesListProps>) {
    const allSelected =
        servicesAvailable.length > 0 && selectedServices.length === servicesAvailable.length;

    return (
        <section className={className}>
            <div className="flex flex-wrap items-center justify-between gap-2 mb-4">
                <h3 className="text-foreground text-sm font-semibold uppercase tracking-wider opacity-80">
                    {heading}
                </h3>

                <Button
                    type="button"
                    variant="ghost"
                    size="sm"
                    onClick={onSelectAll}
                    className="text-primary hover:text-accent text-sm transition-colors px-0"
                >
                    {allSelected ? "Deselect All" : "Select All"}
                </Button>
            </div>

            <div className="space-y-3">
                {servicesAvailable.map((service) => (
                    <label
                        key={service.id}
                        className="flex items-center gap-3 w-full p-4 bg-background rounded-lg border border-border hover:border-primary/40 transition-all cursor-pointer focus-within:ring-2 focus-within:ring-primary focus-within:ring-offset-2"
                    >
                        <input
                            type="checkbox"
                            checked={selectedServices.includes(service.id)}
                            onChange={() => onServiceToggle(service.id)}
                            className="w-4 h-4 rounded border-border bg-background text-primary focus:ring-2 focus:ring-primary"
                        />

                        <span className="text-foreground font-medium">{service.name}</span>
                    </label>
                ))}
            </div>
        </section>
    );
}
