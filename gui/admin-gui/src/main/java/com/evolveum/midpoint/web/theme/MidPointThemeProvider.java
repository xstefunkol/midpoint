package com.evolveum.midpoint.web.theme;

import de.agilecoders.wicket.core.settings.ITheme;
import de.agilecoders.wicket.core.settings.ThemeProvider;

import java.util.Arrays;
import java.util.List;

/**
 * @author lazyman
 */
public class MidPointThemeProvider implements ThemeProvider {

    private ITheme theme = new MidPointTheme();

    @Override
    public ITheme byName(String name) {
        return theme;
    }

    @Override
    public List<ITheme> available() {
        return Arrays.asList(new ITheme[]{theme});
    }

    @Override
    public ITheme defaultTheme() {
        return theme;
    }
}
