/*
 * Copyright (C) 2010 Brockmann Consult GmbH (info@brockmann-consult.de)
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see http://www.gnu.org/licenses/
 */

package org.esa.beam.scripting.visat.actions;

import org.esa.beam.scripting.visat.ScriptConsoleTopComponent;
import org.esa.snap.tango.TangoIcons;

import java.awt.event.ActionEvent;

public class RunAction extends ScriptConsoleAction {
    public static final String ID = "scriptConsole.run";

    public RunAction(ScriptConsoleTopComponent scriptConsoleTC) {
        super(scriptConsoleTC, "Run", ID, TangoIcons.actions_media_playback_start(TangoIcons.Res.R16));
    }

    public void actionPerformed(ActionEvent actionEvent) {
        getScriptConsoleTopComponent().runScript();
    }
}
