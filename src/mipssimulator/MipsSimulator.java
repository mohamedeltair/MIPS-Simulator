
package mipssimulator;

import java.util.*;
class AllValues {
    protected int[]values = new int[86], registers = new int[32];
    protected boolean read;
    public AllValues(int[] values, int[] registers, boolean read) {
        this.values = values;
        this.registers = registers;
        this.read = read;
    }
}
class IFID {
    protected int pcPlus4, inst[] = new int[32];
}
class IDEX {
    protected int regDst, ALUOp,func, ALUSrc, jal, beq, bne, memWrite, memRead, regWrite, pcPlus4, readData1, readData2, signEx[]=new int[32], rt, rd, shamt, memToReg, unsigned; 
}
class EXMEM {
    protected int regWrite, memRead, pcPlus4, memWrite, ALUResult, readData2, writeRegMux, memToReg, jal, writeRegMux2;
}
class MEMWB {
    protected int regWrite, readData, pcPlus4, ALUResult, writeRegMux, memToReg, jal, writeRegMux2;
}
class RegFile {
    protected int[] regs = new int[32];
    RegFile() {
        regs[29] = 2000000000;
    }
}
class DataMemElement {
    protected int location, val;
    public DataMemElement(int location, int val) {
        this.location = location;
        this.val = val;
    }
}
class Ins {
    protected String name;
    protected int rs, rt, rd, shamt, imm;
    protected String address;

    public Ins(String name, int rs, int rt, int rd, int shamt, int imm, String address) {
        this.name = name;
        this.rs = rs;
        this.rt = rt;
        this.rd = rd;
        this.shamt = shamt;
        this.imm = imm;
        this.address = address;
    }

    
    
}
class Lbl {
    protected String name;
    protected int address;

    public Lbl(String name, int address) {
        this.name = name;
        this.address = address;
    }
    
}
class InvalidAddressException extends RuntimeException {
    public InvalidAddressException(String m) {
        super(m);
    }
}
class DataPath {
    protected int firstPc, pc, pcPlus4, inst[]=new int[32], rs, op, rt, rd, imm[] = new int[16], signEx[] = new int[32], readData1, readData2, shamt, regDst, jal, beq, bne, memRead, memToReg, ALUOp, memWrite, ALUSrc, regWrite, immShift, secAdder, writeRegMux, ALUControl, readMux, ALUResult, zeroFlag, PCSrc, readData, wbMux, bits25to0[] = new int[26], concat, branchMux, func, notZeroFlag,unsigned, jumpMux, jrMux, writeRegMux2, wbMux2, jOrJal, jr, jumpAddress[]=new int[28], tempOp, pcPlus4f4bits;
    protected IFID ifid = new IFID();
    protected IDEX idex = new IDEX();
    protected EXMEM exmem = new EXMEM();
    protected MEMWB memwb = new MEMWB();
    protected ArrayList <DataMemElement> dataMem = new ArrayList();
    protected ArrayList <Ins> insMem = new ArrayList();
    protected RegFile regFile = new RegFile();
    protected boolean read=false;
    protected ArrayList<Lbl> lbls = new ArrayList();
    protected ArrayList<AllValues> allV = new ArrayList();
    public int binToDec(int[] bin, boolean signed) {
        int dec=0;
        for (int i=0; i<bin.length; i++) {
            if(i==bin.length-1 && signed==true)
                dec+=-(bin[i]*Math.pow(2,i));
            else
                dec+=(bin[i]*Math.pow(2,i));
        }
        return dec;
    }
    public int[] decToBin(int val, int size) {
        int[] bin = new int[size];
        boolean negative = false;
        if(val < 0) {
            val = -val;
            negative = true;
        }
        for(int i=0; i<size; i++) {
            bin[i] = val%2;
            val/=2;
        }
        if(negative == true) {
            boolean oneMet = false;
            for(int i=0; i<size; i++) {
                if(oneMet==false) {
                    if((bin[i])==1)
                        oneMet = true;
                }
                else
                    bin[i]=bin[i]==1?0:1;
            }
        }
        return bin;
    }
    public int[] signEx(int[] bin) {
        int[] extended = new int[32];
        for(int i=0; i<16; i++)
            extended[i]=bin[i];
        for(int i=16; i<32; i++)
            extended[i]=bin[15];
        return extended;
    }
    public int[] shiftL(int[] bin, int shamt) {
        int[] shifted = new int[32];
        for(int i=0; i<shamt; i++)
            shifted[i]=0;
        for(int i=shamt; i<32; i++)
            shifted[i] = bin[i-shamt];
        return shifted;
    }
    public void wbStage() {
        memwb.ALUResult=exmem.ALUResult;
        memwb.memToReg = exmem.memToReg;
        memwb.readData = readData;
        memwb.regWrite = exmem.regWrite;
        memwb.writeRegMux = exmem.writeRegMux;
        memwb.jal=exmem.jal;
        memwb.writeRegMux2=exmem.writeRegMux2;
        memwb.pcPlus4 = exmem.pcPlus4;
        wbMux=memwb.memToReg==0?memwb.readData:memwb.ALUResult;
        wbMux2=memwb.jal==1?memwb.pcPlus4:wbMux;
        if(memwb.writeRegMux2!=0)
            regFile.regs[memwb.writeRegMux2]=memwb.regWrite==0?regFile.regs[memwb.writeRegMux2]:wbMux2;
    }
    public void memStage() throws InvalidAddressException {
        exmem.ALUResult=ALUResult;
        exmem.memRead = idex.memRead;
        exmem.memToReg = idex.memToReg;
        exmem.memWrite = idex.memWrite;
        exmem.readData2 = idex.readData2;
        exmem.regWrite = idex.regWrite;
        exmem.writeRegMux = writeRegMux;
        exmem.jal = idex.jal;
        exmem.writeRegMux2 = writeRegMux2;
        exmem.pcPlus4=idex.pcPlus4;
        if(!(exmem.memRead==0&&exmem.memWrite==0)) {
            if(!(exmem.ALUResult>=0 && exmem.ALUResult%4==0))
                throw new InvalidAddressException("invalid address");
            if(exmem.memRead==1) {
                int temp = 0;
                for(int i=0; i<dataMem.size(); i++)
                    if((dataMem.get(i)).location==exmem.ALUResult) {
                        temp = (dataMem.get(i)).val;
                        break;
                    }
                read = true;
                readData = temp;
            }
            else {
                boolean found = false;
                for(int i=0; i<dataMem.size(); i++)
                    if((dataMem.get(i)).location==exmem.ALUResult) {
                       (dataMem.get(i)).val = exmem.readData2;
                       found = true;
                       break;
                    }
                if(found==false)
                    dataMem.add(new DataMemElement(exmem.ALUResult,exmem.readData2));
                read = false;
            }
        }
        else read = false;
    }
    public void exStage() {
        idex.ALUOp = ALUOp;
        idex.ALUSrc = ALUSrc;
        idex.beq = beq;
        idex.bne = bne;
        idex.memRead = memRead;
        idex.memToReg = memToReg;
        idex.memWrite = memWrite;
        idex.pcPlus4 = ifid.pcPlus4;
        idex.rd = rd;
        idex.readData1 = readData1;
        idex.readData2 = readData2;
        idex.regDst = regDst;
        idex.regWrite = regWrite;
        idex.rt = rt;
        idex.shamt = shamt;
        idex.signEx = Arrays.copyOfRange(signEx,0,32);
        idex.unsigned = unsigned;
        idex.func = func;
        idex.jal = jal;
        immShift = binToDec(shiftL(idex.signEx,2),true);
        secAdder=idex.pcPlus4+immShift;
        readMux = idex.ALUSrc==0?idex.readData2:binToDec(idex.signEx, true);
        writeRegMux=idex.regDst==0?idex.rt:idex.rd;
        writeRegMux2=idex.jal==1?31:writeRegMux;
        switch(idex.ALUOp) {
        case 0: ALUControl=2; break;
        case 1: ALUControl=6; break;
        case 2:
            if(idex.func==0)
                ALUControl = 3;
            else if(idex.func==32)
                ALUControl = 2;
            else if(idex.func==34)
                ALUControl = 6;
            else if(idex.func==36)
                ALUControl = 0;
            else if(idex.func==37)
                ALUControl = 1;
            else if(idex.func==42 || idex.func==41)
                ALUControl = 7;
            else if(idex.func==39)
                ALUControl = 5;
            break;
        case 3:
            ALUControl = 7;
        }
        switch(ALUControl) {
            case 2: ALUResult=idex.readData1+readMux; break;
            case 6: ALUResult=idex.readData1-readMux; break;
            case 3: ALUResult=binToDec(shiftL(decToBin(readMux, 32), idex.shamt), true); break;
            case 4: 
            case 0: ALUResult = idex.readData1&readMux; break;
            case 1: ALUResult = idex.readData1|readMux; break;
            case 7:
                if(idex.unsigned==0)
                    ALUResult = idex.readData1<readMux?1:0;
                else
                    ALUResult = binToDec(decToBin(idex.readData1,32),false) < binToDec(decToBin(readMux,32),false)?1:0;
                break;
                
            case 5: ALUResult = ~(idex.readData1|readMux); break;
        }
        zeroFlag = ALUResult==0?1:0;
        notZeroFlag = zeroFlag==1?0:1;
        PCSrc = ((idex.beq==1 && zeroFlag==1)||(idex.bne==1&&notZeroFlag==1))?1:0;
    }
    public void idStage() {
        ifid.inst=Arrays.copyOfRange(inst, 0, 32);
        ifid.pcPlus4 = pcPlus4;
        op = binToDec(Arrays.copyOfRange(ifid.inst, 26, 32), false);
        func = binToDec(Arrays.copyOfRange(ifid.inst, 0, 6), false);
        rs = binToDec(Arrays.copyOfRange(ifid.inst, 21 , 26), false);
        rt = binToDec(Arrays.copyOfRange(ifid.inst, 16, 21), false);
        rd = binToDec(Arrays.copyOfRange(ifid.inst, 11, 16), false);
        shamt = binToDec(Arrays.copyOfRange(ifid.inst, 6, 11), false);
        imm = Arrays.copyOfRange(inst, 0, 16);
        signEx= signEx(imm);
        switch(op) {
            case 0: regDst=1; ALUSrc=0; ALUOp=2; beq=bne=jal=0; regWrite=1; memRead=memWrite=0; memToReg=1; break;
            case 8: regDst=0; ALUSrc=1; ALUOp=0; beq=bne=jal=0; regWrite=1; memRead=memWrite=0; memToReg=1; break;
            case 35: regDst=0; ALUSrc=1; ALUOp=0; beq=bne=jal=0; regWrite=1; memRead=1; memWrite=0; memToReg=0; break;
            case 43: ALUSrc=1; ALUOp=0; beq=bne=jal=0; regWrite=0; memRead=0; memWrite=1; break;
            case 4: ALUSrc=0; ALUOp=1; beq=1; bne=jal=0; regWrite=0; memRead=0; memWrite=0; break;
            case 5: ALUSrc=0; ALUOp=1; bne=1; beq=jal=0; regWrite=0; memRead=0; memWrite=0; break;
            case 2: beq=bne=jal=0; regWrite=0; memRead=0; memWrite=0; break;
            case 3: beq=bne=0; jal=1; regWrite=1; memRead=0; memWrite=0; break;
            case 10: regDst=0; ALUSrc=1; beq=bne=jal=0; regWrite=1; memRead=memWrite=0; memToReg=1; ALUOp=3; break;
            case 9: regDst=0; ALUSrc=1; beq=bne=jal=0; regWrite=1; memRead=memWrite=0; memToReg=1; ALUOp=3; break;
        }
        if((op==0&&func==41)||op==9)
            unsigned = 1;
        else unsigned =0;
        readData1 = regFile.regs[rs];
        readData2= regFile.regs[rt];
        if(op==0&&func==8)
            jr=1;
        else
            jr=0;
    }
    public void rfIns(int[]funcArr, int[]shamtArr, int[]rdArr, int[]rtArr, int[]rsArr) {
        int[]opArr = {0,0,0,0,0,0};
        System.arraycopy(funcArr, 0, inst, 0, 6);
        System.arraycopy(shamtArr, 0, inst, 6, 5);
        System.arraycopy(rdArr, 0, inst, 11, 5);
        System.arraycopy(rtArr, 0, inst, 16, 5);
        System.arraycopy(rsArr, 0, inst, 21, 5);
        System.arraycopy(opArr, 0, inst, 26, 6);
    }
    public void ifIns(int[] immArr, int[]rtArr, int[] rsArr, int[] opArr) {
        System.arraycopy(immArr, 0, inst, 0, 16);
        System.arraycopy(rtArr, 0, inst, 16, 5);
        System.arraycopy(rsArr, 0, inst, 21, 5);
        System.arraycopy(opArr, 0, inst, 26, 6);
    }
    public void jfIns(int[] addressArr, int[] opArr) {
        System.arraycopy(addressArr, 0, inst, 0, 26);
        System.arraycopy(opArr, 0, inst, 26, 6);
    }
    public void ifStage() {
        pc=jrMux;
        pcPlus4=pc+4;
        branchMux = PCSrc==1?secAdder:pcPlus4;
        if(pc>=firstPc && pc<firstPc+(insMem.size()+4)*4){
            if(pc<firstPc+(insMem.size())*4) {
                Ins ins = (Ins)insMem.get((pc-firstPc)/4);
                switch(ins.name) {
                    case "add":
                        int[] addShamt = {0,0,0,0,0};
                        int[] addFunc= {0,0,0,0,0,1};
                        rfIns(addFunc,addShamt,decToBin(ins.rd, 5), decToBin(ins.rt, 5),decToBin(ins.rs, 5));
                        break;
                    case "sub":
                        int[] subShamt = {0,0,0,0,0};
                        int[] subFunc= {0,1,0,0,0,1};
                        rfIns(subFunc,subShamt,decToBin(ins.rd, 5), decToBin(ins.rt, 5),decToBin(ins.rs, 5));
                        break;
                    case "sll":
                        int[] sllFunc= {0,0,0,0,0,0};
                        rfIns(sllFunc,decToBin(ins.shamt, 5),decToBin(ins.rd, 5), decToBin(ins.rt, 5),decToBin(ins.rs, 5));
                        break;
                    case "and":
                        int[] andShamt = {0,0,0,0,0};
                        int[] andFunc= {0,0,1,0,0,1};
                        rfIns(andFunc,andShamt,decToBin(ins.rd, 5), decToBin(ins.rt, 5),decToBin(ins.rs, 5));
                        break;
                    case "or":
                        int[] orShamt = {0,0,0,0,0};
                        int[] orFunc= {1,0,1,0,0,1};
                        rfIns(orFunc,orShamt,decToBin(ins.rd, 5), decToBin(ins.rt, 5),decToBin(ins.rs, 5));
                        break;
                    case "nor":
                        int[] norShamt = {0,0,0,0,0};
                        int[] norFunc= {1,1,1,0,0,1};
                        rfIns(norFunc,norShamt,decToBin(ins.rd, 5), decToBin(ins.rt, 5),decToBin(ins.rs, 5));
                        break;
                    case "jr":
                        int[] jrShamt = {0,0,0,0,0};
                        int[] jrFunc= {0,0,0,1,0,0};
                        rfIns(jrFunc,jrShamt,decToBin(ins.rd, 5), decToBin(ins.rt, 5),decToBin(ins.rs, 5));
                        break;
                    case "slt":
                        int[] sltShamt = {0,0,0,0,0};
                        int[] sltFunc= {0,1,0,1,0,1};
                        rfIns(sltFunc,sltShamt,decToBin(ins.rd, 5), decToBin(ins.rt, 5),decToBin(ins.rs, 5));
                        break;
                    case "sltu":
                        int[] sltuShamt = {0,0,0,0,0};
                        int[] sltuFunc= {1,0,0,1,0,1};
                        rfIns(sltuFunc,sltuShamt,decToBin(ins.rd, 5), decToBin(ins.rt, 5),decToBin(ins.rs, 5));
                        break;
                    case "addi":
                        int[] addiOp = {0,0,0,1,0,0};
                        ifIns(decToBin(ins.imm, 16), decToBin(ins.rt, 5), decToBin(ins.rs, 5), addiOp);
                        break;
                    case "lw":
                        int[] lwOp = {1,1,0,0,0,1};
                        ifIns(decToBin(ins.imm, 16), decToBin(ins.rt, 5), decToBin(ins.rs, 5), lwOp);
                        break;
                    case "sw":
                        int[] swOp = {1,1,0,1,0,1};
                        ifIns(decToBin(ins.imm, 16), decToBin(ins.rt, 5), decToBin(ins.rs, 5), swOp);
                        break;
                    case "beq":
                        int[] beqOp = {0,0,1,0,0,0};
                        int ra=0;
                        for(int i = 0; i<lbls.size(); i++)
                            if(lbls.get(i).name.equals(ins.address)) {
                                int aa = lbls.get(i).address;
                                if(aa>pc)
                                    ra= aa - (pc/4) - 1;
                                else
                                    ra=-((pc/4)-aa+1);
                                break;
                            }
                        ifIns(decToBin(ra, 16), decToBin(ins.rt, 5), decToBin(ins.rs, 5), beqOp);
                        break;
                    case "bne":
                        int[] bneOp = {1,0,1,0,0,0};
                        int ra2=0;
                        for(int i = 0; i<lbls.size(); i++)
                            if(lbls.get(i).name.equals(ins.address)) {
                                int aa = lbls.get(i).address;
                                if(aa>pc)
                                    ra2= aa - (pc/4) - 1;
                                else
                                    ra2=-((pc/4)-aa+1);
                                break;
                            }
                        ifIns(decToBin(ra2, 16), decToBin(ins.rt, 5), decToBin(ins.rs, 5), bneOp);
                        break;
                    case "slti":
                        int[] sltiOp = {0,1,0,1,0,0};
                        ifIns(decToBin(ins.imm, 16), decToBin(ins.rt, 5), decToBin(ins.rs, 5), sltiOp);
                        break;
                    case "sltui":
                        int[] sltuiOp = {1,0,0,1,0,0};
                        ifIns(decToBin(ins.imm, 16), decToBin(ins.rt, 5), decToBin(ins.rs, 5), sltuiOp);
                        break;
                    case "j":
                        int[] jOp = {0,1,0,0,0,0};
                        int aa=0;
                        for(int i = 0; i<lbls.size(); i++)
                            if(lbls.get(i).name.equals(ins.address)) {
                                aa = lbls.get(i).address;
                                break;
                            }
                        jfIns(decToBin(aa, 26), jOp);
                        break;
                    case "jal":
                        int[] jalOp = {1,1,0,0,0,0};
                        int aa2=0;
                        for(int i = 0; i<lbls.size(); i++)
                            if(lbls.get(i).name.equals(ins.address)) {
                                aa2 = lbls.get(i).address;
                                break;
                            }
                        jfIns(decToBin(aa2, 26), jalOp);
                        break;
                }
            }
            else {
                int[] noOpFunc={0,0,0,0,0,0};
                int[] noOpField={0,0,0,0,0};
                rfIns(noOpFunc, noOpField, noOpField, noOpField, noOpField);
            }
            tempOp=binToDec(Arrays.copyOfRange(inst, 26, 32),false);
            System.arraycopy(inst, 0, bits25to0, 0, 26);
            int[] twoZeros = {0,0};
            System.arraycopy(twoZeros, 0, jumpAddress, 0, 2);
            System.arraycopy(bits25to0, 0, jumpAddress, 2, 26);
            int[] tempAddress = new int[32];
            int[] pcPlus4FirstBits=Arrays.copyOfRange(decToBin(pcPlus4, 32),28,32);
            pcPlus4f4bits = binToDec(pcPlus4FirstBits, false);
            System.arraycopy(pcPlus4FirstBits, 0, tempAddress, 28, 4);
            System.arraycopy(jumpAddress, 0, tempAddress, 0, 28);
            concat = binToDec(tempAddress,false);
            if(tempOp==2||tempOp==3)
                jOrJal=1;
            else
                jOrJal = 0;
            if(jOrJal==1)
                jumpMux=concat;
            else
                jumpMux=branchMux;
            if(jr==1)
                jrMux=readData1;
            else
                jrMux=jumpMux;
        }
    }
    public void clockCycle() {
        wbStage();
        memStage();
        exStage();
        idStage();
        ifStage();
    }
    public void simProgram() {
        while(jrMux>=firstPc && jrMux<firstPc+(insMem.size()+4)*4) {
            clockCycle();
            int[] arr = {pc,ifid.pcPlus4, binToDec(ifid.inst, false),idex.regDst, idex.ALUOp,idex.func, idex.ALUSrc, idex.jal, idex.beq, idex.bne, idex.memWrite, idex.memRead, idex.regWrite, idex.pcPlus4, idex.readData1, idex.readData2, binToDec(idex.signEx, true), idex.rt, idex.rd, idex.shamt, idex.memToReg, idex.unsigned,exmem.regWrite, exmem.memRead, exmem.pcPlus4, exmem.memWrite, exmem.ALUResult, exmem.readData2, exmem.writeRegMux, exmem.memToReg, exmem.jal, exmem.writeRegMux2,memwb.regWrite, memwb.readData, memwb.pcPlus4, memwb.ALUResult, memwb.writeRegMux, memwb.memToReg, memwb.jal, memwb.writeRegMux2, pcPlus4, binToDec(inst,false), rs, op, rt, rd, binToDec(imm, true), binToDec(signEx,true), readData1, readData2, shamt, regDst, jal, beq, bne, memRead, memToReg, ALUOp, memWrite, ALUSrc, regWrite, immShift, secAdder, writeRegMux, ALUControl, readMux, ALUResult, zeroFlag, PCSrc, readData, wbMux, binToDec(bits25to0, false), concat, branchMux, func, notZeroFlag,unsigned, jumpMux, jrMux, writeRegMux2, wbMux2, jOrJal, jr, binToDec(jumpAddress, false), tempOp, pcPlus4f4bits};
            int[] arr2 = Arrays.copyOf(regFile.regs, 32);
            allV.add(new AllValues(arr, arr2, read));
            } 
    }
    
}

public class MipsSimulator {

    public static void main(String[] args) {
        Frame1 f = new Frame1();
        f.setVisible(true);
    }
    
}
