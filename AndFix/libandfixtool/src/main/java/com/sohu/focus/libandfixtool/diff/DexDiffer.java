package com.sohu.focus.libandfixtool.diff;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

import org.jf.dexlib2.DexFileFactory;
import org.jf.dexlib2.dexbacked.DexBackedClassDef;
import org.jf.dexlib2.dexbacked.DexBackedDexFile;
import org.jf.dexlib2.dexbacked.DexBackedField;
import org.jf.dexlib2.dexbacked.DexBackedMethod;
import org.jf.dexlib2.dexbacked.DexBackedMethodImplementation;
import org.jf.dexlib2.dexbacked.DexBackedTryBlock;
import org.jf.dexlib2.iface.instruction.Instruction;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;

public class DexDiffer {
    public DiffInfo diff(File newFile, File oldFile)
            throws IOException {
        DexBackedDexFile newDexFile = DexFileFactory.loadDexFile(newFile, 19,
                true);
        DexBackedDexFile oldDexFile = DexFileFactory.loadDexFile(oldFile, 19,
                true);

        DiffInfo info = DiffInfo.getInstance();

        boolean contains = false;
        for (DexBackedClassDef newClazz : newDexFile.getClasses()) {
            Set<DexBackedClassDef> oldclasses = (Set<DexBackedClassDef>) oldDexFile.getClasses();
            for (DexBackedClassDef oldClazz : oldclasses) {
                if (newClazz.equals(oldClazz)) {
                    compareField(newClazz, oldClazz, info);
                    compareMethod(newClazz, oldClazz, info);
                    contains = true;
                    break;
                }
            }
            if (contains)
                continue;
            info.addAddedClasses(newClazz);
        }

        return info;
    }

    public void compareMethod(DexBackedClassDef newClazz, DexBackedClassDef oldClazz, DiffInfo info) {
        compareMethod(newClazz.getMethods(), oldClazz.getMethods(), info);
    }

    public void compareMethod(Iterable<? extends DexBackedMethod> news, Iterable<? extends DexBackedMethod> olds, DiffInfo info) {
        for (DexBackedMethod reference : news) {
            if (reference.getName().equals("<clinit>")) {
                continue;
            }
            compareMethod(reference, olds, info);
        }
    }

    public void compareMethod(DexBackedMethod object, Iterable<? extends DexBackedMethod> olds, DiffInfo info) {
        for (DexBackedMethod reference : olds) {
            if (!reference.equals(object)) {
                continue;
            }
            if ((reference.getImplementation() == null) &&
                    (object.getImplementation() != null)) {
                info.addModifiedMethods(object);
                return;
            }
            if ((reference.getImplementation() != null) &&
                    (object.getImplementation() == null)) {
                info.addModifiedMethods(object);
                return;
            }
            if ((reference.getImplementation() == null) &&
                    (object.getImplementation() == null)) {
                return;
            }

            if (!compareImplementation(reference.getImplementation(),object.getImplementation())) {
                info.addModifiedMethods(object);
                return;
            }
            return;
        }

        info.addAddedMethods(object);
    }

    private boolean compareImplementation(DexBackedMethodImplementation one, DexBackedMethodImplementation two) {
        if (one.getRegisterCount() == two.getRegisterCount()) {
            if (equalTryBlocks(one.getTryBlocks(),two.getTryBlocks())) {
                if (equalParameterNames(one.getInstructions(),two.getInstructions()))
                    return true;
            }
        }
        return false;
    }

    /**
     * try块是否相等
     * @param a
     * @param b
     * @return
     */
    private boolean equalTryBlocks(List<? extends DexBackedTryBlock> a, List<? extends DexBackedTryBlock> b)
    {
        if (a.size() != b.size()) {
            return false;
        }

        for (int i = 0; i < a.size(); i++) {
            DexBackedTryBlock at = (DexBackedTryBlock)a.get(i);
            DexBackedTryBlock bt = (DexBackedTryBlock)b.get(i);
            if (!at.equals(bt)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 指令列表中的每一个指令是否相等
     * @param ai
     * @param bi
     * @return
     */
    private boolean equalParameterNames(Iterable<? extends Instruction> ai, Iterable<? extends Instruction> bi)
    {
        ImmutableList a = ImmutableList.copyOf(ai);
        ImmutableList b = ImmutableList.copyOf(bi);

        if (a.size() != b.size()) {
            return false;
        }

        for (int i = 0; i < a.size(); i++) {
            Instruction at = (Instruction)a.get(i);
            Instruction bt = (Instruction)b.get(i);
            if (at.getOpcode()!=bt.getOpcode()) {
                return false;
            }
        }
        return true;
    }

    public void compareField(DexBackedClassDef newClazz, DexBackedClassDef oldClazz, DiffInfo info) {
        compareField(Iterables.concat(newClazz.getStaticFields(), newClazz.getInstanceFields()), Iterables.concat(oldClazz.getStaticFields(), oldClazz.getInstanceFields()), info);
    }

    public void compareField(Iterable<? extends DexBackedField> news, Iterable<? extends DexBackedField> olds, DiffInfo info) {
        for (DexBackedField reference : news)
            compareField(reference, olds, info);
    }

    public void compareField(DexBackedField object, Iterable<? extends DexBackedField> olds, DiffInfo info) {
        for (DexBackedField reference : olds) {
            if (reference.equals(object)) {
                if ((reference.getInitialValue() == null) &&
                        (object.getInitialValue() != null)) {
                    info.addModifiedFields(object);
                    return;
                }
                if ((reference.getInitialValue() != null) &&
                        (object.getInitialValue() == null)) {
                    info.addModifiedFields(object);
                    return;
                }
                if ((reference.getInitialValue() == null) &&
                        (object.getInitialValue() == null)) {
                    return;
                }
                if (reference.getInitialValue().compareTo(
                        object.getInitialValue()) != 0) {
                    info.addModifiedFields(object);
                    return;
                }
                return;
            }
        }

        info.addAddedFields(object);
    }
}
