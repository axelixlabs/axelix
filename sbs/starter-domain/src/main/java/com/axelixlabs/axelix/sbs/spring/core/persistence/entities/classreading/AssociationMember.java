/*
 * Copyright (C) 2025-2026 Axelix Labs
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.axelixlabs.axelix.sbs.spring.core.persistence.entities.classreading;

import java.lang.reflect.Member;
import java.util.Objects;

/**
 * An association identified by its name and the Java member used to access it.
 *
 * @author Nikita Kirilloff
 */
public final class AssociationMember {

    private final String name;
    private final Member member;

    /**
     * @param name the association name
     * @param member the field or accessor method associated with it
     */
    public AssociationMember(String name, Member member) {
        this.name = Objects.requireNonNull(name, "name");
        this.member = Objects.requireNonNull(member, "member");
    }

    public String name() {
        return name;
    }

    public Member member() {
        return member;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof AssociationMember)) {
            return false;
        }
        AssociationMember that = (AssociationMember) other;
        return name.equals(that.name) && member.equals(that.member);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, member);
    }
}
