-- Catalog seed: predefined repair-type issue (issue_master). Asset links null = available as generic choice.
INSERT INTO issue_master (
    issue_title,
    issue_description,
    category_id,
    sub_category_id,
    component_id,
    spare_part_id,
    created_by,
    active
) VALUES (
    'Repair / service request',
    'Standard repair or maintenance request for an asset. Add details when you submit the ticket.',
    NULL,
    NULL,
    NULL,
    NULL,
    'system',
    TRUE
);
