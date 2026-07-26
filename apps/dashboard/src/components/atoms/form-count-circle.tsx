export function FormCountCircle({ count }: { readonly count: number }) {
    return (
        <div className="flex size-8 items-center justify-center rounded-full bg-primary text-sm font-semibold text-primary-foreground">
            {count}
        </div>
    );
}
