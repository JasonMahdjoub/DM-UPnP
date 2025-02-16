/*
 * Copyright (C) 2013 4th Line GmbH, Switzerland
 *
 * The contents of this file are subject to the terms of either the GNU
 * Lesser General Public License Version 2 or later ("LGPL") or the
 * Common Development and Distribution License Version 1 or later
 * ("CDDL") (collectively, the "License"). You may not use this file
 * except in compliance with the License. See LICENSE.txt for more
 * information.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package com.distrimind.upnp.model.meta;

import com.distrimind.upnp.model.ModelUtil;
import com.distrimind.upnp.model.Validatable;
import com.distrimind.upnp.model.ValidationError;
import com.distrimind.upnp.model.types.Datatype;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import com.distrimind.flexilogxml.log.DMLogger;
import com.distrimind.upnp.Log;

/**
 * Describes a single action argument, either input or output.
 * <p>
 * No, I haven't  figured out so far what the "return value" thingy is good for.
 * </p>
 *
 * @author Christian Bauer
 */
public class ActionArgument<S extends Service<?, ?, ?>> implements Validatable {

    final private static DMLogger log = Log.getLogger(ActionArgument.class);

    public enum Direction {
        IN, OUT
    }

    final private String name;
    final private Collection<String> aliases;
    final private String relatedStateVariableName;
    final private Direction direction;
    final private boolean returnValue;     // TODO: What is this stuff good for anyway?

    // Package mutable state
    private Action<S> action;

    public ActionArgument(String name, String relatedStateVariableName, Direction direction) {
        this(name, Collections.emptyList(), relatedStateVariableName, direction, false);
    }

    public ActionArgument(String name, Collection<String> aliases, String relatedStateVariableName, Direction direction) {
        this(name, aliases, relatedStateVariableName, direction, false);
    }
    public ActionArgument(String name, String[] aliases, String relatedStateVariableName, Direction direction) {
        this(name, List.of(aliases), relatedStateVariableName, direction, false);
    }

    public ActionArgument(String name, String relatedStateVariableName, Direction direction, boolean returnValue) {
        this(name, Collections.emptyList(), relatedStateVariableName, direction, returnValue);
    }

    public ActionArgument(String name, Collection<String> aliases, String relatedStateVariableName, Direction direction, boolean returnValue) {
        this.name = name;
        this.aliases = List.copyOf(aliases);
        this.relatedStateVariableName = relatedStateVariableName;
        this.direction = direction;
        this.returnValue = returnValue;
    }

    public String getName() {
        return name;
    }

    public Collection<String> getAliases() {
        return aliases;
    }

    public boolean isNameOrAlias(String name) {
        if (getName().equalsIgnoreCase(name)) return true;
        for (String alias : aliases) {
            if (alias.equalsIgnoreCase(name)) return true;
        }
        return false;
    }

    public String getRelatedStateVariableName() {
        return relatedStateVariableName;
    }

    public Direction getDirection() {
        return direction;
    }

    public boolean isReturnValue() {
        return returnValue;
    }

    public Action<S> getAction() {
        return action;
    }

    void setAction(Action<S> action) {
        if (this.action != null)
            throw new IllegalStateException("Final value has been set already, model is immutable");
        this.action = action;
    }

    public Datatype<?> getDatatype() {
        return getAction().getService().getDatatype(this);
    }

    @Override
	public List<ValidationError> validate() {
        List<ValidationError> errors = new ArrayList<>();

        if (getName() == null || getName().isEmpty()) {
            errors.add(new ValidationError(
                    getClass(),
                    "name",
                    "Argument without name of: " + getAction()
            ));
        } else if (log.isWarnEnabled()) {

            if (!ModelUtil.isValidUDAName(getName())) {
                if (log.isWarnEnabled()) {
                    log.warn(Icon.UPN_P_SPECIFICATION_VIOLATION_OF + getAction().getService().getDevice());
                    log.warn("Invalid argument name: " + this);
                }
            } else if (getName().length() > 32) {
                if (log.isWarnEnabled()) {
                    log.warn(Icon.UPN_P_SPECIFICATION_VIOLATION_OF + getAction().getService().getDevice());
                    log.warn("Argument name should be less than 32 characters: " + this);
                }
            }

        }

        if (getDirection() == null) {
            errors.add(new ValidationError(
                    getClass(),
                    "direction",
                    "Argument '"+getName()+"' requires a direction, either IN or OUT"
            ));
        }

        if (isReturnValue() && getDirection() != ActionArgument.Direction.OUT) {
            errors.add(new ValidationError(
                    getClass(),
                    "direction",
                    "Return value argument '" + getName() + "' must be direction OUT"
            ));
        }

        return errors;
    }

    public ActionArgument<S> deepCopy() {
        return new ActionArgument<>(
                getName(),
                getAliases(),
                getRelatedStateVariableName(),
                getDirection(),
                isReturnValue()
        );
    }

    @Override
    public String toString() {
        return "(" + getClass().getSimpleName() + ", " + getDirection() + ") " + getName();
    }
}
