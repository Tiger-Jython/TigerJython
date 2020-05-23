/*
 * This file is part of the 'TigerJython' project.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tigerjython.plugins;

/**
 * We define an interface for a `(Int, Int, String)=>Unit` function for easy interaction with Jython.
 *
 * @author Tobias Kohn
 */
public interface ErrorNotification {

    void apply(long time, int line, int column, String message);
}
