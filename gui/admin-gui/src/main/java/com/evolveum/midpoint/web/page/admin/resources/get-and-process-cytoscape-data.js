function getAndProcessCytoscapeData() {
    console.log("getAndProcessCytoscapeData starting123");
    var xhttp = new XMLHttpRequest();
    xhttp.open("GET", "${callbackUrl}", true);
    xhttp.setRequestHeader("Content-type", "application/json", true);
    xhttp.onreadystatechange = function() {
        var text = xhttp.responseText;
        console.log("server response123: " + text);
        process_cytoscape_data(xhttp.responseText);
    }
    xhttp.send(null);
    console.log("getAndProcessCytoscapeData finished123");
}