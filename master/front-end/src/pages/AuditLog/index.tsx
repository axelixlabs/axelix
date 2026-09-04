import { useEffect, useState } from "react";
import { PagesFirstSection } from "@/components";
import { AuditLogSearchBar } from "./AuditLogSearchBar";
import { AuditLogTable } from "./AuditLogTable";
import { Pagination } from "antd";
import { PAGINATION_SIZE } from "@/utils";

const AuditLog = () => {
    const [currentPage, setCurrentPage] = useState<number>(1);
    const [search, setSearch] = useState<string>("");
    const [filters, setFilters] = useState<any>({})

    useEffect(() => {
        setCurrentPage(1);
    }, [search, filters]);

    return (
        <>
            <PagesFirstSection title="Audit Log" subtitle="Here you can track and monitor all activities and changes that occur within the system." />

            <AuditLogSearchBar addonAfter="Placeholder" setSearch={setSearch} />

            <AuditLogTable />

            <Pagination
                current={currentPage}
                pageSize={PAGINATION_SIZE}
                total={100}
                hideOnSinglePage
                showSizeChanger={false}
                onChange={setCurrentPage}
            />
        </>
    );
};

export default AuditLog;