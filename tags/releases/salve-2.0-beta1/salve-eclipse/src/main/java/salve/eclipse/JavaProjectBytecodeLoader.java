/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package salve.eclipse;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

import salve.Bytecode;
import salve.BytecodeLoader;
import salve.eclipse.builder.FileBytecodeLoader;

public class JavaProjectBytecodeLoader implements BytecodeLoader
{

    private final IJavaProject project;

    public JavaProjectBytecodeLoader(IProject project) throws JavaModelException
    {
        this.project = JavaCore.create(project);
    }

    public Bytecode loadBytecode(String className)
    {
        if (className == null)
        {
            throw new IllegalArgumentException("Argument `className` cannot be null");
        }
        try
        {
            IType type = findType(className);
            if (type == null)
            {
                for (IPackageFragmentRoot root : project.getPackageFragmentRoots())
                {
                    IPath out = root.getRawClasspathEntry().getOutputLocation();
                    if (out == null)
                    {
                        out = project.getJavaProject().getOutputLocation();
                    }
                    IFolder output = project.getJavaProject().getProject().getWorkspace().getRoot()
                            .getFolder(out);
                    IFile file = output.getFile(new Path(className + ".class"));
                    if (file.exists())
                    {
                        FileBytecodeLoader loader = new FileBytecodeLoader(file);
                        Bytecode bytecode = loader.loadBytecode(className);
                        if (bytecode == null)
                        {
                            return null;
                        }
                        else
                        {
                            return new Bytecode(className, bytecode.getBytes(), this);
                        }
                    }
                }
            }

            final IClassFile classFile = type.getClassFile();

            if (classFile != null)
            {
                byte[] bytecode = classFile.getBytes();
                if (bytecode != null)
                {
                    return new Bytecode(className, bytecode, this);
                }
                else
                {
                    return null;
                }
            }


            IPath out = type.getJavaProject().getOutputLocation();
            IFolder output = type.getJavaProject().getProject().getWorkspace().getRoot().getFolder(
                    out);
            IFile file = output.getFile(new Path(className + ".class"));

            if (!file.exists())
            {
                IClasspathEntry[] cp = type.getJavaProject().getRawClasspath();
                for (IClasspathEntry entry : cp)
                {
                    out = entry.getOutputLocation();
                    if (out != null)
                    {
                        output = type.getJavaProject().getProject().getWorkspace().getRoot()
                                .getFolder(out);
                        file = output.getFile(new Path(className + ".class"));
                        if (file.exists())
                        {
                            break;
                        }
                    }
                }
            }

            if (file == null)
            {
                return null;
            }
            else
            {
                FileBytecodeLoader loader = new FileBytecodeLoader(file);
                Bytecode bytecode = loader.loadBytecode(className);
                if (bytecode == null)
                {
                    return null;
                }
                else
                {
                    return new Bytecode(className, bytecode.getBytes(), this);
                }
            }
        }
        catch (Exception e)
        {
            Activator.getDefault().getLog().log(
                    new Status(Status.ERROR, Activator.PLUGIN_ID,
                            "Could not load bytecode for class: " + className, e));
            return null;
        }
    }

    private IType findType(String className)
    {
        String cn = className.replace("/", ".");

        try
        {
            IType type = project.getJavaProject().findType(cn);
            if (type == null)
            {
                cn = cn.replace("$", ".");
                type = project.getJavaProject().findType(cn);
            }
            return type;
        }
        catch (JavaModelException e)
        {
            throw new RuntimeException("Error resolving type for class: " + className, e);
        }
    }

}
