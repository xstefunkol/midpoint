function getAndProcessCytoscapeData() {
    console.log("getAndProcessCytoscapeData starting");
    var xhttp = new XMLHttpRequest();
    xhttp.open("GET", "${callbackUrl}", false, "administrator", "5ecr3t");       // TODO fix this hack
    xhttp.setRequestHeader("Content-type", "application/json");
    xhttp.onreadystatechange = function() {
        if (xhttp.readyState == 4 && xhttp.status == 200) {
            var text = xhttp.responseText;
            console.log("server response: " + text);
            process_cytoscape_data(xhttp.responseText);
        }
    };
    xhttp.send(null);
    console.log("getAndProcessCytoscapeData finished");
}