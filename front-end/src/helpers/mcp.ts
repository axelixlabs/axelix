export const filterMCPTools = (MCPTools: any[], search: string): any[] => {
    const formattedSearch = search.toLowerCase().trim();

    return MCPTools.filter(({ title }) => {
        const lowerName = title.toLowerCase();
        if (lowerName.includes(formattedSearch)) {
            return true;
        }
    });
};