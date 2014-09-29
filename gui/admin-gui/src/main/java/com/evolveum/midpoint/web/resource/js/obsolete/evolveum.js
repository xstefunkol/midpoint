/*
 * Copyright (c) 2010-2013 Evolveum
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * DEPRECATED [lazyman]
 */

var interval = 0;
var ajaxError = 0;

window.onload = initAjaxStatusSigns;
$(document).ready(function(){
	init();

/*	if (Wicket.Ajax) {
		Wicket.Ajax.registerPostCallHandler(MyApp.Ajax.firePostHandlers);
	}*/
});

function isIE9OrNewer() {
    //todo not working with jquery > 1.9 [lazyman]
//    return $.browser.msie && $.browser.version >= 9.0;
    return true;
}

function isIE() {
    //todo not working with jquery > 1.9 [lazyman]
//    return $.browser.msie;
    return false;
}

function init() {
	$(document).unbind("mousedown");
	$("#blackWindow").css("opacity", .8);
	$("#blackWindow").hide();
	$("#xmlExport").hide();
	
	if (!isIE9OrNewer()){
		$(".acc .acc-section").css("height", "1px");
		$(".acc-content .sortedTable table").css("width", $(".acc-content").width());
	}

	var el = $('.searchPanel');
    el.focus(function(e) {
        if (e.target.value == e.target.defaultValue)
            e.target.value = '';
    });
    el.blur(function(e) {
        if (e.target.value == '')
            e.target.value = e.target.defaultValue;
    });
    
    $(".searchText").keypress(function(e) {
        if(e.which == 13) {
        	$(this).parent().find(".submitSearch").click();
        }
    });
    
    $(document).bind("mousedown", function(e) {
		if ($("#xmlExport").has(e.target).length === 0) {
			$("#blackWindow").hide();
			$("#xmlExport").hide();
		}
	});
}


function clickFuncWicket6(eventData) {
    var clickedElement = (window.event) ? event.srcElement : eventData.target;
    if ((clickedElement.tagName.toUpperCase() == 'BUTTON' || clickedElement.tagName.toUpperCase() == 'A' || clickedElement.parentNode.tagName.toUpperCase() == 'A'
        || (clickedElement.tagName.toUpperCase() == 'INPUT' && (clickedElement.type.toUpperCase() == 'BUTTON' || clickedElement.type.toUpperCase() == 'SUBMIT')))
        && clickedElement.parentNode.id.toUpperCase() != 'NOBUSY' ) {
        showAjaxStatusSign();
    }
}

function initAjaxStatusSigns() {
    document.getElementsByTagName('body')[0].onclick = clickFuncWicket6;
    hideAjaxStatusSign();
    Wicket.Event.subscribe('/ajax/call/beforeSend', function( attributes, jqXHR, settings ) {
        showAjaxStatusSign();
    });
    Wicket.Event.subscribe('/ajax/call/complete', function( attributes, jqXHR, textStatus) {
        hideAjaxStatusSign();
    });
}

function showAjaxStatusSign() {
    document.getElementById('ajax_busy').style.display = 'inline';
}

function hideAjaxStatusSign() {
    document.getElementById('ajax_busy').style.display = 'none';
}

function initXml(xml) {
	var output = "<small>*to hide the window, please click on the black background</small><h1>XML report</h1>" + xml;
	$("#xmlExportContent").html(output);
	$("#xmlExport").show();
	$("#blackWindow").show();
}

function setupFunc() {
	document.getElementsByTagName('body')[0].onclick = clickFunc;
	hideBusysign();

    Wicket.Event.subscribe('/ajax/call/before', function(jqEvent, attributes, jqXHR, errorThrown, textStatus) {
        // This dropClickEvent(attributes) is here because of selectable data table checkbox.
        // That checkbox trigger showBusysign but then event dropped in precondition and busy sign will not be hidden.
        if (!dropClickEvent(attributes)) {
            showBusysign();
        }
    });
    Wicket.Event.subscribe('/ajax/call/after', function(jqEvent, attributes, jqXHR, errorThrown, textStatus) {
        hideBusysign();
    });
    Wicket.Event.subscribe('/ajax/call/failure', function(jqEvent, attributes, jqXHR, errorThrown, textStatus) {
        showError();
    });
}

function hideBusysign() {
//	document.getElementById('bysy_indicator').style.display = 'none';
//	document.getElementById('error_indicator').style.display = 'none';
//	hideDisableOperationFormButtons();
//	hideDisablePaging();
	ajaxError = 0;
}

function showError() {
//	document.getElementById('bysy_indicator').style.display = 'none';
//	document.getElementById('error_indicator').style.display = 'inline';
//	showDisableOperationFormButtons();
	ajaxError = 1;
}

function showBusysign() {
//	if(ajaxError != 1) {
//		document.getElementById('bysy_indicator').style.display = 'inline';
//		document.getElementById('error_indicator').style.display = 'none';
//	}
}

var clickedElement = null;

function clickFunc(eventData) {
	clickedElement = (window.event) ? event.srcElement : eventData.target;
	if (clickedElement.tagName.toUpperCase() == 'BUTTON'
			|| clickedElement.tagName.toUpperCase() == 'A'
			|| clickedElement.parentNode.tagName.toUpperCase() == 'A'
			|| (clickedElement.tagName.toUpperCase() == 'INPUT' && (clickedElement.type
					.toUpperCase() == 'BUTTON' || clickedElement.type
					.toUpperCase() == 'SUBMIT'))) {
		showBusysign();
	}
}

if(clickedElement != null) {
	if ((clickedElement.tagName.toUpperCase() == 'A'
		&& ((clickedElement.target == null) || (clickedElement.target.length <= 0))
		&& (clickedElement.href.lastIndexOf('#') != (clickedElement.href.length - 1))
		&& (!('nobusy' in clickedElement))
		&& (clickedElement.href.indexOf('skype') < 0)
		&& (clickedElement.href.indexOf('mailto') < 0)
		&& (clickedElement.href.indexOf('WicketAjaxDebug') < 0)
		&& (clickedElement.href.lastIndexOf('.doc') != (clickedElement.href.length - 4))
		&& (clickedElement.href.lastIndexOf('.csv') != (clickedElement.href.length - 4))
		&& (clickedElement.href.lastIndexOf('.xls') != (clickedElement.href.length - 4)) && ((clickedElement.onclick == null) || (clickedElement.onclick
		.toString().indexOf('window.open') <= 0)))
		|| (clickedElement.parentNode.tagName.toUpperCase() == 'A'
				&& ((clickedElement.parentNode.target == null) || (clickedElement.parentNode.target.length <= 0))
				&& (clickedElement.parentNode.href.indexOf('skype') < 0)
				&& (clickedElement.parentNode.href.indexOf('mailto') < 0)
				&& (clickedElement.parentNode.href.lastIndexOf('#') != (clickedElement.parentNode.href.length - 1))
				&& (clickedElement.parentNode.href.lastIndexOf('.doc') != (clickedElement.parentNode.href.length - 4))
				&& (clickedElement.parentNode.href.lastIndexOf('.csv') != (clickedElement.parentNode.href.length - 4))
				&& (clickedElement.parentNode.href.lastIndexOf('.xls') != (clickedElement.parentNode.href.length - 4)) && ((clickedElement.parentNode.onclick == null) || (clickedElement.parentNode.onclick
				.toString().indexOf('window.open') <= 0)))
		|| (((clickedElement.onclick == null) || ((clickedElement.onclick
				.toString().indexOf('confirm') <= 0)
				&& (clickedElement.onclick.toString().indexOf('alert') <= 0) && (clickedElement.onclick
				.toString().indexOf('Wicket.Palette') <= 0))) && (clickedElement.tagName
				.toUpperCase() == 'INPUT' && (clickedElement.type.toUpperCase() == 'BUTTON'
				|| clickedElement.type.toUpperCase() == 'SUBMIT' || clickedElement.type
				.toUpperCase() == 'IMAGE')))) {
	showBusysign();
	}
}

/**
 * Method provides checks for full row click support - it decides which events should be dropped - we
 * want to stop event bubbling when user clicks for example on input event (then row shouldn't change
 * it's row selected status)
 *
 * @param attrs
 * @return {boolean} true if event should be dropped
 */
function dropClickEvent(attrs) {
    var evt = attrs.event;

    //if clicked on <tr>
    if (evt.target == evt.currentTarget) {
        return false;
    }
    //if clicked on <td> which is a child of <tr>
    if (evt.target.parentNode == evt.currentTarget) {
        return false;
    }

    //we drop event if it input or link
    var targetElement = evt.target.nodeName.toLowerCase();
    var isInput = (targetElement == 'input' || targetElement == 'select' || targetElement == 'option');
    var isLink = (targetElement == 'a');
    if (isInput || isLink) {
        return true;
    }

    return false;
}