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
import org.jf.dexlib2.dexbacked.instruction.DexBackedArrayPayload;
import org.jf.dexlib2.dexbacked.instruction.DexBackedInstruction10t;
import org.jf.dexlib2.dexbacked.instruction.DexBackedInstruction11n;
import org.jf.dexlib2.dexbacked.instruction.DexBackedInstruction11x;
import org.jf.dexlib2.dexbacked.instruction.DexBackedInstruction12x;
import org.jf.dexlib2.dexbacked.instruction.DexBackedInstruction20bc;
import org.jf.dexlib2.dexbacked.instruction.DexBackedInstruction20t;
import org.jf.dexlib2.dexbacked.instruction.DexBackedInstruction21c;
import org.jf.dexlib2.dexbacked.instruction.DexBackedInstruction21ih;
import org.jf.dexlib2.dexbacked.instruction.DexBackedInstruction21lh;
import org.jf.dexlib2.dexbacked.instruction.DexBackedInstruction21s;
import org.jf.dexlib2.dexbacked.instruction.DexBackedInstruction21t;
import org.jf.dexlib2.dexbacked.instruction.DexBackedInstruction22b;
import org.jf.dexlib2.dexbacked.instruction.DexBackedInstruction22c;
import org.jf.dexlib2.dexbacked.instruction.DexBackedInstruction22cs;
import org.jf.dexlib2.dexbacked.instruction.DexBackedInstruction22s;
import org.jf.dexlib2.dexbacked.instruction.DexBackedInstruction22t;
import org.jf.dexlib2.dexbacked.instruction.DexBackedInstruction22x;
import org.jf.dexlib2.dexbacked.instruction.DexBackedInstruction23x;
import org.jf.dexlib2.dexbacked.instruction.DexBackedInstruction25x;
import org.jf.dexlib2.dexbacked.instruction.DexBackedInstruction30t;
import org.jf.dexlib2.dexbacked.instruction.DexBackedInstruction31c;
import org.jf.dexlib2.dexbacked.instruction.DexBackedInstruction31i;
import org.jf.dexlib2.dexbacked.instruction.DexBackedInstruction31t;
import org.jf.dexlib2.dexbacked.instruction.DexBackedInstruction32x;
import org.jf.dexlib2.dexbacked.instruction.DexBackedInstruction35c;
import org.jf.dexlib2.dexbacked.instruction.DexBackedInstruction35mi;
import org.jf.dexlib2.dexbacked.instruction.DexBackedInstruction35ms;
import org.jf.dexlib2.dexbacked.instruction.DexBackedInstruction3rc;
import org.jf.dexlib2.dexbacked.instruction.DexBackedInstruction3rmi;
import org.jf.dexlib2.dexbacked.instruction.DexBackedInstruction3rms;
import org.jf.dexlib2.dexbacked.instruction.DexBackedInstruction51l;
import org.jf.dexlib2.dexbacked.instruction.DexBackedPackedSwitchPayload;
import org.jf.dexlib2.dexbacked.instruction.DexBackedSparseSwitchPayload;
import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.iface.instruction.SwitchElement;

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
            System.out.println(newClazz.toString());
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
            if(reference.getName().equals("getInfo"))
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
            if (!cmpareInstruction(at,bt)) {
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

    private boolean commonCompareInstruction(Instruction a,Instruction b){
        return a.getOpcode()==b.getOpcode();
    }

    //比较指令
    private boolean cmpareInstruction(Instruction a,Instruction b){
        if(!a.getClass().equals(b.getClass())) return false;
        if(a instanceof DexBackedInstruction10t){
            boolean condition1 = commonCompareInstruction(a,b);
            boolean condition2 = (((DexBackedInstruction10t) a).getCodeOffset() == ((DexBackedInstruction10t)b).getCodeOffset());
            return condition1 && condition2;
        }else if(a instanceof DexBackedInstruction11n){
            boolean condition1 = commonCompareInstruction(a,b);
            boolean condition2 = ((DexBackedInstruction11n)a).getRegisterA()==((DexBackedInstruction11n)b).getRegisterA();
            boolean condition3 = ((DexBackedInstruction11n)a).getNarrowLiteral()==((DexBackedInstruction11n)b).getNarrowLiteral();
            boolean condition4 = Long.compare(((DexBackedInstruction11n)a).getWideLiteral(),((DexBackedInstruction11n)b).getWideLiteral()) == 0;
            return condition1&&condition2&&condition3&&condition4;
        }else if(a instanceof DexBackedInstruction11x){
            boolean condition1 = commonCompareInstruction(a,b);
            boolean condition2 = ((DexBackedInstruction11x)a).getRegisterA()==((DexBackedInstruction11x)b).getRegisterA();
            return condition1&&condition2;
        }else if(a instanceof DexBackedInstruction12x){
            boolean condition1 = commonCompareInstruction(a,b);
            boolean condition2 = ((DexBackedInstruction12x)a).getRegisterA()==((DexBackedInstruction12x)b).getRegisterA();
            boolean condition3 = ((DexBackedInstruction12x)a).getRegisterB()==((DexBackedInstruction12x)b).getRegisterB();
            return condition1&&condition2&&condition3;
        }else if(a instanceof DexBackedInstruction20bc){
            boolean condition1 = commonCompareInstruction(a,b);
            boolean condition2 = ((DexBackedInstruction20bc)a).getVerificationError()==((DexBackedInstruction20bc)b).getVerificationError();
            boolean condition3 = ((DexBackedInstruction20bc)a).getReferenceType()==((DexBackedInstruction20bc)b).getReferenceType();
            boolean condition4 = ((DexBackedInstruction20bc)a).getReference().equals(((DexBackedInstruction20bc)b).getReference());
            return condition1&&condition2&&condition3&&condition4;
        }else if(a instanceof DexBackedInstruction20t){
            boolean condition1 = commonCompareInstruction(a,b);
            boolean condition2 = ((DexBackedInstruction20t)a).getCodeOffset()==((DexBackedInstruction20t)b).getCodeOffset();
            return condition1&&condition2;
        }else if(a instanceof DexBackedInstruction21c){
            boolean condition1 = commonCompareInstruction(a,b);
            boolean condition2 = ((DexBackedInstruction21c)a).getRegisterA()==((DexBackedInstruction21c)b).getRegisterA();
            boolean condition3 = ((DexBackedInstruction21c)a).getReference().equals(((DexBackedInstruction21c)b).getReference());
            return condition1&&condition2&&condition3;
        }else if(a instanceof DexBackedInstruction21ih){
            boolean condition1 = commonCompareInstruction(a,b);
            boolean condition2 = ((DexBackedInstruction21ih)a).getRegisterA()==((DexBackedInstruction21ih)b).getRegisterA();
            boolean condition3 = ((DexBackedInstruction21ih)a).getNarrowLiteral()==((DexBackedInstruction21ih)b).getNarrowLiteral();
            boolean condition4 = Long.compare(((DexBackedInstruction21ih) a).getWideLiteral(), ((DexBackedInstruction21ih) b).getWideLiteral()) == 0;
            boolean condition5 = ((DexBackedInstruction21ih)a).getHatLiteral()==((DexBackedInstruction21ih)b).getHatLiteral();
            return condition1&&condition2&&condition3&&condition4&&condition5;
        }else if(a instanceof DexBackedInstruction21lh){
            boolean condition1 = commonCompareInstruction(a,b);
            boolean condition2 = ((DexBackedInstruction21lh)a).getRegisterA()==((DexBackedInstruction21ih)b).getRegisterA();
            boolean condition3 = Long.compare(((DexBackedInstruction21lh) a).getWideLiteral(), ((DexBackedInstruction21lh) b).getWideLiteral()) == 0;
            boolean condition4 = ((DexBackedInstruction21lh)a).getHatLiteral()==((DexBackedInstruction21lh)b).getHatLiteral();
            return condition1&&condition2&&condition3&&condition4;
        }else if(a instanceof DexBackedInstruction21s){
            boolean condition1 = commonCompareInstruction(a,b);
            boolean condition2 = ((DexBackedInstruction21s)a).getRegisterA()==((DexBackedInstruction21s)b).getRegisterA();
            boolean condition3 = Long.compare(((DexBackedInstruction21s) a).getWideLiteral(), ((DexBackedInstruction21s) b).getWideLiteral()) == 0;
            boolean condition4 = ((DexBackedInstruction21s)a).getNarrowLiteral()==((DexBackedInstruction21s)b).getNarrowLiteral();
            return condition1&&condition2&&condition3&&condition4;
        }else if(a instanceof DexBackedInstruction21t){
            boolean condition1 = commonCompareInstruction(a,b);
            boolean condition2 = ((DexBackedInstruction21t)a).getRegisterA()==((DexBackedInstruction21t)b).getRegisterA();
            boolean condition3 = ((DexBackedInstruction21t)a).getCodeOffset()==((DexBackedInstruction21t)b).getCodeOffset();
            return condition1&&condition2&&condition3;
        }else if(a instanceof DexBackedInstruction22b){
            boolean condition1 = commonCompareInstruction(a,b);
            boolean condition2 = ((DexBackedInstruction22b)a).getRegisterA()==((DexBackedInstruction22b)b).getRegisterA();
            boolean condition3 = Long.compare(((DexBackedInstruction22b) a).getWideLiteral(), ((DexBackedInstruction22b) b).getWideLiteral()) == 0;
            boolean condition4 = ((DexBackedInstruction22b)a).getNarrowLiteral()==((DexBackedInstruction22b)b).getNarrowLiteral();
            boolean condition5 = ((DexBackedInstruction22b)a).getRegisterB()==((DexBackedInstruction22b)b).getRegisterB();
            return condition1&&condition2&&condition3&&condition4&&condition5;
        }else if(a instanceof DexBackedInstruction22c){
            boolean condition1 = commonCompareInstruction(a,b);
            boolean condition2 = ((DexBackedInstruction22c)a).getRegisterA()==((DexBackedInstruction22c)b).getRegisterA();
            boolean condition3 = ((DexBackedInstruction22c)a).getRegisterB()==((DexBackedInstruction22c)b).getRegisterB();
            boolean condition4 = ((DexBackedInstruction22c)a).getReference().equals(((DexBackedInstruction22c)b).getReference());
            return condition1&&condition2&&condition3&&condition4;
        }else if(a instanceof DexBackedInstruction22cs){
            boolean condition1 = commonCompareInstruction(a,b);
            boolean condition2 = ((DexBackedInstruction22cs)a).getRegisterA()==((DexBackedInstruction22cs)b).getRegisterA();
            boolean condition3 = ((DexBackedInstruction22cs)a).getRegisterB()==((DexBackedInstruction22cs)b).getRegisterB();
            boolean condition4 = ((DexBackedInstruction22cs)a).getFieldOffset()==((DexBackedInstruction22cs)b).getFieldOffset();
            return condition1&&condition2&&condition3&&condition4;
        }else if(a instanceof DexBackedInstruction22s){
            boolean condition1 = commonCompareInstruction(a,b);
            boolean condition2 = ((DexBackedInstruction22s)a).getRegisterA()==((DexBackedInstruction22s)b).getRegisterA();
            boolean condition3 = ((DexBackedInstruction22s)a).getRegisterB()==((DexBackedInstruction22s)b).getRegisterB();
            boolean condition4 = ((DexBackedInstruction22s)a).getNarrowLiteral()==((DexBackedInstruction22s)b).getNarrowLiteral();
            boolean condition5 = Long.compare(((DexBackedInstruction22s) a).getWideLiteral(), ((DexBackedInstruction22s) b).getWideLiteral()) == 0;
            return condition1&&condition2&&condition3&&condition4&&condition5;
        }else if(a instanceof DexBackedInstruction22x){
            boolean condition1 = commonCompareInstruction(a,b);
            boolean condition2 = ((DexBackedInstruction22x)a).getRegisterA()==((DexBackedInstruction22x)b).getRegisterA();
            boolean condition3 = ((DexBackedInstruction22x)a).getRegisterB()==((DexBackedInstruction22x)b).getRegisterB();
            return condition1&&condition2&&condition3;
        }else if(a instanceof DexBackedInstruction23x){
            boolean condition1 = commonCompareInstruction(a,b);
            boolean condition2 = ((DexBackedInstruction23x)a).getRegisterA()==((DexBackedInstruction23x)b).getRegisterA();
            boolean condition3 = ((DexBackedInstruction23x)a).getRegisterB()==((DexBackedInstruction23x)b).getRegisterB();
            boolean condition4 = ((DexBackedInstruction23x)a).getRegisterC()==((DexBackedInstruction23x)b).getRegisterC();
            return condition1&&condition2&&condition3&&condition4;
        }else if(a instanceof DexBackedInstruction25x){
            boolean condition1 = commonCompareInstruction(a,b);
            boolean condition2 = ((DexBackedInstruction25x)a).getParameterRegisterCount()==((DexBackedInstruction25x)b).getParameterRegisterCount();
            boolean condition3 = ((DexBackedInstruction25x)a).getRegisterFixedC()==((DexBackedInstruction25x)b).getRegisterFixedC();
            boolean condition4 = ((DexBackedInstruction25x)a).getRegisterParameterD()==((DexBackedInstruction25x)b).getRegisterParameterD();
            boolean condition5 = ((DexBackedInstruction25x)a).getRegisterParameterE()==((DexBackedInstruction25x)b).getRegisterParameterE();
            boolean condition6 = ((DexBackedInstruction25x)a).getRegisterParameterF()==((DexBackedInstruction25x)b).getRegisterParameterF();
            boolean condition7 = ((DexBackedInstruction25x)a).getRegisterParameterG()==((DexBackedInstruction25x)b).getRegisterParameterG();
            return condition1&&condition2&&condition3&&condition4&&condition5&&condition6&&condition7;
        }else if(a instanceof DexBackedInstruction30t){
            boolean condition1 = commonCompareInstruction(a,b);
            boolean condition2 = ((DexBackedInstruction30t)a).getCodeOffset()==((DexBackedInstruction30t)b).getCodeOffset();
            return condition1&&condition2;
        }else if(a instanceof DexBackedInstruction31c){
            boolean condition1 = commonCompareInstruction(a,b);
            boolean condition2 = ((DexBackedInstruction31c)a).getRegisterA()==((DexBackedInstruction31c)b).getRegisterA();
            boolean condition3 = ((DexBackedInstruction31c)a).getReference().equals(((DexBackedInstruction31c)b).getReference());
            return condition1&&condition2&&condition3;
        }else if(a instanceof DexBackedInstruction31i){
            boolean condition1 = commonCompareInstruction(a,b);
            boolean condition2 = ((DexBackedInstruction31i)a).getRegisterA()==((DexBackedInstruction31i)b).getRegisterA();
            boolean condition3 = ((DexBackedInstruction31i)a).getNarrowLiteral()==((DexBackedInstruction31i)b).getNarrowLiteral();
            return condition1&&condition2&&condition3;
        }else if(a instanceof DexBackedInstruction31t){
            boolean condition1 = commonCompareInstruction(a,b);
            boolean condition2 = ((DexBackedInstruction31t)a).getRegisterA()==((DexBackedInstruction31t)b).getRegisterA();
            boolean condition3 = ((DexBackedInstruction31t)a).getCodeOffset()==((DexBackedInstruction31t)b).getCodeOffset();
            return condition1&&condition2&&condition3;
        }else if(a instanceof DexBackedInstruction32x){
            boolean condition1 = commonCompareInstruction(a,b);
            boolean condition2 = ((DexBackedInstruction32x)a).getRegisterA()==((DexBackedInstruction32x)b).getRegisterA();
            boolean condition3 = ((DexBackedInstruction32x)a).getRegisterB()==((DexBackedInstruction32x)b).getRegisterB();
            return condition1&&condition2&&condition3;
        }else if(a instanceof DexBackedInstruction35c){
            boolean condition1 = commonCompareInstruction(a,b);
            boolean condition2 = ((DexBackedInstruction35c)a).getRegisterCount()==((DexBackedInstruction35c)b).getRegisterCount();
            boolean condition3 = ((DexBackedInstruction35c)a).getRegisterC()==((DexBackedInstruction35c)b).getRegisterC();
            boolean condition4 = ((DexBackedInstruction35c)a).getRegisterD()==((DexBackedInstruction35c)b).getRegisterD();
            boolean condition5 = ((DexBackedInstruction35c)a).getRegisterE()==((DexBackedInstruction35c)b).getRegisterE();
            boolean condition6 = ((DexBackedInstruction35c)a).getRegisterF()==((DexBackedInstruction35c)b).getRegisterF();
            boolean condition7 = ((DexBackedInstruction35c)a).getRegisterG()==((DexBackedInstruction35c)b).getRegisterG();
            boolean condition8 = ((DexBackedInstruction35c)a).getReference().equals(((DexBackedInstruction35c) b).getReference());
            return condition1&&condition2&&condition3&&condition4&&condition5&&condition6&&condition7&&condition8;
        }else if(a instanceof DexBackedInstruction35mi){
            boolean condition1 = commonCompareInstruction(a,b);
            boolean condition2 = ((DexBackedInstruction35mi)a).getRegisterCount()==((DexBackedInstruction35c)b).getRegisterCount();
            boolean condition3 = ((DexBackedInstruction35mi)a).getRegisterC()==((DexBackedInstruction35mi)b).getRegisterC();
            boolean condition4 = ((DexBackedInstruction35mi)a).getRegisterD()==((DexBackedInstruction35mi)b).getRegisterD();
            boolean condition5 = ((DexBackedInstruction35mi)a).getRegisterE()==((DexBackedInstruction35mi)b).getRegisterE();
            boolean condition6 = ((DexBackedInstruction35mi)a).getRegisterF()==((DexBackedInstruction35mi)b).getRegisterF();
            boolean condition7 = ((DexBackedInstruction35mi)a).getRegisterG()==((DexBackedInstruction35mi)b).getRegisterG();
            boolean condition8 = ((DexBackedInstruction35mi)a).getInlineIndex()==((DexBackedInstruction35mi) b).getInlineIndex();
            return condition1&&condition2&&condition3&&condition4&&condition5&&condition6&&condition7&&condition8;
        }else if(a instanceof DexBackedInstruction35ms){
            boolean condition1 = commonCompareInstruction(a,b);
            boolean condition2 = ((DexBackedInstruction35ms)a).getRegisterCount()==((DexBackedInstruction35ms)b).getRegisterCount();
            boolean condition3 = ((DexBackedInstruction35ms)a).getRegisterC()==((DexBackedInstruction35ms)b).getRegisterC();
            boolean condition4 = ((DexBackedInstruction35ms)a).getRegisterD()==((DexBackedInstruction35ms)b).getRegisterD();
            boolean condition5 = ((DexBackedInstruction35ms)a).getRegisterE()==((DexBackedInstruction35ms)b).getRegisterE();
            boolean condition6 = ((DexBackedInstruction35ms)a).getRegisterF()==((DexBackedInstruction35ms)b).getRegisterF();
            boolean condition7 = ((DexBackedInstruction35ms)a).getRegisterG()==((DexBackedInstruction35ms)b).getRegisterG();
            boolean condition8 = ((DexBackedInstruction35ms)a).getVtableIndex()==((DexBackedInstruction35ms) b).getVtableIndex();
            return condition1&&condition2&&condition3&&condition4&&condition5&&condition6&&condition7&&condition8;
        }else if(a instanceof DexBackedInstruction3rc){
            boolean condition1 = commonCompareInstruction(a,b);
            boolean condition2 = ((DexBackedInstruction3rc)a).getRegisterCount()==((DexBackedInstruction3rc)b).getRegisterCount();
            boolean condition3 = ((DexBackedInstruction3rc)a).getStartRegister()==((DexBackedInstruction3rc)b).getStartRegister();
            boolean condition4 = ((DexBackedInstruction3rc)a).getReference().equals(((DexBackedInstruction3rc) b).getReference());
            return condition1&&condition2&&condition3&&condition4;
        }else if(a instanceof DexBackedInstruction3rmi){
            boolean condition1 = commonCompareInstruction(a,b);
            boolean condition2 = ((DexBackedInstruction3rmi)a).getRegisterCount()==((DexBackedInstruction3rmi)b).getRegisterCount();
            boolean condition3 = ((DexBackedInstruction3rmi)a).getStartRegister()==((DexBackedInstruction3rmi)b).getStartRegister();
            boolean condition4 = ((DexBackedInstruction3rmi)a).getInlineIndex()==((DexBackedInstruction3rmi) b).getInlineIndex();
            return condition1&&condition2&&condition3&&condition4;
        }else if(a instanceof DexBackedInstruction3rms){
            boolean condition1 = commonCompareInstruction(a,b);
            boolean condition2 = ((DexBackedInstruction3rms)a).getRegisterCount()==((DexBackedInstruction3rms)b).getRegisterCount();
            boolean condition3 = ((DexBackedInstruction3rms)a).getStartRegister()==((DexBackedInstruction3rms)b).getStartRegister();
            boolean condition4 = ((DexBackedInstruction3rms)a).getVtableIndex()==((DexBackedInstruction3rms) b).getVtableIndex();
            return condition1&&condition2&&condition3&&condition4;
        }else if(a instanceof DexBackedInstruction51l){
            boolean condition1 = commonCompareInstruction(a,b);
            boolean condition2 = ((DexBackedInstruction51l)a).getRegisterA()==((DexBackedInstruction51l)b).getRegisterA();
            boolean condition3 = ((DexBackedInstruction51l)a).getWideLiteral()==((DexBackedInstruction51l)b).getWideLiteral();
            return condition1&&condition2&&condition3;
        }else if(a instanceof DexBackedArrayPayload){
            boolean condition1 = commonCompareInstruction(a,b);
            boolean condition2 = ((DexBackedArrayPayload)a).getElementWidth()==((DexBackedArrayPayload)b).getElementWidth();
            boolean condition3 = ((DexBackedArrayPayload)a).getCodeUnits()==((DexBackedArrayPayload)b).getCodeUnits();
            boolean condition4 = elementsEquals(((DexBackedArrayPayload)a).getArrayElements(),((DexBackedArrayPayload)b).getArrayElements());
            return condition1&&condition2&&condition3&&condition4;
        }else if(a instanceof DexBackedPackedSwitchPayload){
            boolean condition1 = commonCompareInstruction(a,b);
            boolean condition2 = ((DexBackedPackedSwitchPayload)a).getCodeUnits()==((DexBackedPackedSwitchPayload)b).getCodeUnits();
            boolean condition3 = elementsEquals1(((DexBackedPackedSwitchPayload) a).getSwitchElements(), ((DexBackedPackedSwitchPayload) b).getSwitchElements());
            return condition1&&condition2&&condition3;
        }else if(a instanceof DexBackedSparseSwitchPayload){
            boolean condition1 = commonCompareInstruction(a,b);
            boolean condition2 = ((DexBackedSparseSwitchPayload)a).getCodeUnits()==((DexBackedSparseSwitchPayload)b).getCodeUnits();
            boolean condition3 = elementsEquals1(((DexBackedSparseSwitchPayload) a).getSwitchElements(), ((DexBackedSparseSwitchPayload) b).getSwitchElements());
            return condition1&&condition2&&condition3;
        }else{
            return commonCompareInstruction(a,b);
        }
    }


    private boolean elementsEquals(List<Number> a, List<Number> b) {
        if (a.size() != b.size()) {
            return false;
        }

        for (int i = 0; i < a.size(); i++) {
            Number ae = (Number)a.get(i);
            Number be = (Number)b.get(i);
            if (Long.compare(ae.longValue(), be.longValue()) != 0) {
                return false;
            }
        }
        return true;
    }

    private boolean elementsEquals1(List<? extends SwitchElement> a, List<? extends SwitchElement> b)
    {
        if (a.size() != b.size()) {
            return false;
        }

        for (int i = 0; i < a.size(); i++) {
            SwitchElement ae = (SwitchElement)a.get(i);
            SwitchElement be = (SwitchElement)b.get(i);
            if ((ae.getKey() != be.getKey()) || (ae.getOffset() != be.getOffset())) {
                return false;
            }
        }
        return true;
    }
}
