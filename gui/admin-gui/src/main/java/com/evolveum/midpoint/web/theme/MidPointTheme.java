package com.evolveum.midpoint.web.theme;

import de.agilecoders.wicket.core.settings.ITheme;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.request.resource.PackageResourceReference;

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
                new PackageResourceReference(MidPointTheme.class, "bootstrap.css")));
    }

    @Override
    public Iterable<String> getCdnUrls() {
        return null;
    }
}
