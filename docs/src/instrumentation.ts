export async function register() {
    if (process.env.NEXT_RUNTIME === "nodejs" && process.env.NODE_ENV === "production") {
        const { search } = await import("@/lib/search");
        // A search initializes the index without serializing it.
        await search.search("");
    }
}
