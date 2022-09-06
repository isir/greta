/*
 * Copyright (C) 2015-2017, C. Ramakrishnan / Illposed Software.
 * All rights reserved.
 *
 * This code is licensed under the BSD 3-Clause license.
 * SPDX-License-Identifier: BSD-3-Clause
 * See file LICENSE.md for more information.
 */

package com.illposed.osc.argument.handler;

import com.illposed.osc.argument.ArgumentHandler;
import com.illposed.osc.argument.OSCSymbol;
import com.illposed.osc.OSCParseException;
import java.nio.ByteBuffer;
import java.util.Map;

/**
 * Parses and serializes an OSC symbol type.
 */
public class SymbolArgumentHandler implements ArgumentHandler<OSCSymbol>, Cloneable {

	// Public API
	@SuppressWarnings("WeakerAccess")
	public static final char DEFAULT_IDENTIFIER = 'S';

	private final StringArgumentHandler stringArgumentHandler;

	// Public API
	@SuppressWarnings("WeakerAccess")
	public SymbolArgumentHandler() {
		this.stringArgumentHandler = new StringArgumentHandler();
	}

	// Public API
	@SuppressWarnings("unused")
	public StringArgumentHandler getInternalStringArgumentHandler() {
		return stringArgumentHandler;
	}

	@Override
	public char getDefaultIdentifier() {
		return DEFAULT_IDENTIFIER;
	}

	@Override
	public Class<OSCSymbol> getJavaClass() {
		return OSCSymbol.class;
	}

	@Override
	public void setProperties(final Map<String, Object> properties) {
		stringArgumentHandler.setProperties(properties);
	}

	@Override
	public boolean isMarkerOnly() {
		return false;
	}

	@Override
	@SuppressWarnings("unchecked")
	public SymbolArgumentHandler clone() throws CloneNotSupportedException {
		return (SymbolArgumentHandler) super.clone();
	}

	@Override
	public OSCSymbol parse(final ByteBuffer input) throws OSCParseException {
		return OSCSymbol.valueOf(stringArgumentHandler.parse(input));
	}

	@Override
	public void serialize(final ByteBuffer output, final OSCSymbol value) {
		stringArgumentHandler.serialize(output, value.toString());
	}
}
