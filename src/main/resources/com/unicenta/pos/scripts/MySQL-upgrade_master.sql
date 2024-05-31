UPDATE resources SET content = $FILE{/com/unicenta/pos/templates/Printer.Ticket.xml} WHERE name = 'Printer.Ticket';
UPDATE applications SET version = $APP_VERSION{} WHERE id = $APP_ID{};
COMMIT;