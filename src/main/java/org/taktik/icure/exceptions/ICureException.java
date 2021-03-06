/*
 * Copyright (C) 2018 Taktik SA
 *
 * This file is part of iCureBackend.
 *
 * iCureBackend is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2 as published by
 * the Free Software Foundation.
 *
 * iCureBackend is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with iCureBackend.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.taktik.icure.exceptions;

public class ICureException extends Exception {
	private static final long serialVersionUID = -8702376071839870064L;

	public ICureException() {
		super();
	}

	public ICureException(String s, Throwable t) {
		super(s, t);
	}

	public ICureException(String s) {
		super(s);
	}

	public ICureException(Throwable t) {
		super(t);
	}
}