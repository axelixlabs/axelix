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
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Enumeration;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class FactoriesVisibleTest {
    @Test
    public void factoriesOnClasspath() throws Exception {
        Enumeration<URL> urls = getClass().getClassLoader().getResources("META-INF/spring.factories");
        boolean found = false;
        while (urls.hasMoreElements()) {
            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(urls.nextElement().openStream(), "UTF-8"));
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append('\n');
            }
            reader.close();
            if (content.toString().contains("digital.pragmatech.testing.SpringTestProfilerListener")) {
                found = true;
            }
        }
        assertTrue(found);
    }
}
