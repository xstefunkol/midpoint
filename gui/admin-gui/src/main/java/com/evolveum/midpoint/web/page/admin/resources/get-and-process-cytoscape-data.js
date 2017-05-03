function getAndProcessCytoscapeData() {
    console.log("getAndProcessCytoscapeData starting");
    var xhttp = new XMLHttpRequest();
    xhttp.open("GET", "${callbackUrlRetrieve}", false, "administrator", "5ecr3t");       // TODO fix this hack
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

function deleteAttribute(resourceOid, kind, intent, objectClass, attributeName) {
    console.log("deleteAttribute starting");
    var xhttp1 = new XMLHttpRequest();
    var url = "${callbackUrlDelete}&resourceOid=" + resourceOid + "&kind=" + kind + "&intent=" + intent + "&objectClass=" + objectClass + "&attributeName=" + attributeName
    console.log("deleteAttribute: " + url);
    xhttp1.open("GET", url, false, "administrator", "5ecr3t");       // TODO fix this hack
    xhttp1.setRequestHeader("Content-type", "text/plain");
    xhttp1.send(null);
    xhttp1.onreadystatechange = function() {
        console.log("server response (delete action): " + xhttp1.responseText);
    };
    console.log("deleteAttribute finished, going to re-read the resource");
    getAndProcessCytoscapeData();       // re-read the resource
}