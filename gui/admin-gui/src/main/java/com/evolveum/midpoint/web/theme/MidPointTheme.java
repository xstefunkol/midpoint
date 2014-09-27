package com.evolveum.midpoint.web.theme;

import com.evolveum.midpoint.web.resource.css.CssResource;
import com.evolveum.midpoint.web.resource.js.JsResource;
import de.agilecoders.wicket.core.settings.ITheme;
import de.agilecoders.wicket.less.LessResourceReference;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.request.resource.SharedResourceReference;
import org.apache.wicket.request.resource.UrlResourceReference;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author lazyman
 */
public class MidPointTheme implements ITheme {

    @Override
    public String name() {
        return "Gizmo";
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        response.render(CssHeaderItem.forReference(
                new PackageResourceReference(MidPointTheme.class, "bootstrap/bootstrap.css")));

//        response.render(CssHeaderItem.forReference(
//                new PackageResourceReference(MidPointTheme.class, "bootswatch/bootstrap.css")));

        response.render(CssHeaderItem.forReference(
                new SharedResourceReference(CssResource.class, "jquery.easypiechart.css")));

        // bootstrap select, https://github.com/silviomoreto/bootstrap-select/releases
        response.render(CssHeaderItem.forReference(
                new SharedResourceReference(CssResource.class, "bootstrap-multiselect.css")));
        response.render(CssHeaderItem.forReference(
                new SharedResourceReference(CssResource.class, "bootstrap-select.css")));
        response.render(JavaScriptHeaderItem.forReference(
                new SharedResourceReference(JsResource.class, "bootstrap-multiselect.js")));
        response.render(JavaScriptHeaderItem.forReference(
                new SharedResourceReference(JsResource.class, "bootstrap-select.js")));

        // jquery easy pie chart
        response.render(CssHeaderItem.forReference(
                new SharedResourceReference(CssResource.class, "jquery.easypiechart.css")));
        response.render(JavaScriptHeaderItem.forReference(
                new SharedResourceReference(JsResource.class, "jquery.easypiechart.js")));

        // jquery pnotify
        response.render(CssHeaderItem.forReference(
                new SharedResourceReference(CssResource.class, "jquery.pnotify.default.css")));
        response.render(JavaScriptHeaderItem.forReference(
                new SharedResourceReference(JsResource.class, "jquery.pnotify.js")));

        // midpoint theme
        response.render(CssHeaderItem.forReference(
                new LessResourceReference(MidPointTheme.class, "MidPointTheme.less")));

        //todo remove obsolete JS [lazyman]
        response.render(JavaScriptHeaderItem.forReference(
                new SharedResourceReference(JsResource.class, "obsolete/evolveum.js")));
        response.render(JavaScriptHeaderItem.forReference(
                new SharedResourceReference(JsResource.class, "obsolete/ace-editor.js")));
        response.render(JavaScriptHeaderItem.forReference(
                new SharedResourceReference(JsResource.class, "obsolete/dropdown-multiple.js")));
        response.render(JavaScriptHeaderItem.forReference(
                new SharedResourceReference(JsResource.class, "obsolete/midpoint.js")));
    }

    @Override
    public Iterable<String> getCdnUrls() {
        return null;
    }
}
