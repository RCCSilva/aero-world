$(document).ready( function () {
    var tables = $('.datatable');
    for(let i = 0; i < tables.length; i++) {
        try {
            $(tables.get(i)).DataTable();
        } catch (err) {
            console.error('Failed to create Datatable')
        }        
    }
} );