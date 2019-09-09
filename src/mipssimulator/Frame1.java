/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mipssimulator;

import java.util.*;
import javax.swing.JOptionPane;

/**
 *
 * @author elteir
 */

public class Frame1 extends javax.swing.JFrame {
        private DataPath dp;
        private ArrayList <DataMemElement> dm = new ArrayList();
        private int cycle;
        private boolean newP = true;
        public void writeInTables(int ind) {
            AllValues av = dp.allV.get(ind);
            for(int i=0; i<86; i++) {
                if(i==69) {
                    if(av.read==false) {
                        table.setValueAt("No value", i, 1);
                        continue;
                    }
                }
                table.setValueAt(av.values[i], i, 1);
            }
            for(int i=0; i<32; i++) {
                table2.setValueAt(av.registers[i], i, 1);
            }
            txtClk.setText((ind+1)+"");
            cycle = ind+1;
        }
        
        public void takeProgram() {
        Scanner s2 = new Scanner(jTextArea1.getText());
        String st = "";
        boolean lbl = false;
        int i = dp.firstPc/4;
        int rem = 0;
        Scanner sTemp=null;
        try {
        while(true) {
            if((i*4)+16<0)
            {
                JOptionPane.showMessageDialog(null, "too large number for starting PC");
                return;
            }
            if(lbl==true && sTemp.hasNext()) {
                st = st.substring(rem, st.length());
            }
            else if(!s2.hasNext())
                break;
            else while(true) {
                st = s2.nextLine();
                if(!st.trim().equalsIgnoreCase(""))
                    break;
            }
            Scanner s = new Scanner(st);
            s.useDelimiter("(,|\\s*\\s)+");
            int rdT=0, rsT=0, rtT=0, cons=0;
                    String temp = s.next().toLowerCase();
            switch(temp) {
                case "add": case "sub": case "and": case "or": case "nor": case "slt": case "sltu":
                    rdT=whatReg(s.next()); rsT=whatReg(s.next()); rtT=whatReg(s.next());
                    if(rdT>=0 && rdT<=31 &&rsT>=0 && rsT<=31 &&rtT>=0 && rtT<=31)
                        dp.insMem.add(new Ins(temp,rsT, rtT, rdT,0,0,""));
                    else {
                        error();
                        return;
                    }
                    if(lbl==true) {
                        lbl = false;
                        rem = 0;
                    }
                    break;
                case "addi": case "slti": case "sltui":
                    rtT=whatReg(s.next()); rsT=whatReg(s.next()); cons = s.nextInt();
                    if(cons>=-32768 && cons<=32767 &&rsT>=0 && rsT<=31 &&rtT>=0 && rtT<=31)
                        dp.insMem.add(new Ins(temp, rsT, rtT,0,0,cons,""));
                    else {
                        error();
                        return;
                    }
                    if(lbl==true) {
                        lbl = false;
                        rem = 0;
                    }
                    break;
                case "lw": case "sw":
                    rtT=whatReg(s.next());
                    String temp2 = s.nextLine();
                    Scanner sOff = new Scanner(temp2);
                    boolean comma =false;
                    sOff.useDelimiter("(,|\\s*\\s)+");
                    String stTemp="";
                    while(true) {
                        char c = sOff.findInLine(".").charAt(0);
                        if(comma==false && c!=' ' && c!= ',') {
                            error();
                            return;
                        }
                        else if(comma == false && c==',')
                            comma = true;
                        else {
                            if(c!='(')
                                stTemp+=c;
                            else break;
                        }
                    }
                    stTemp = (stTemp.replaceAll(",", " ")).trim();
                    cons = Integer.parseInt(stTemp);
                    String temp3 = "";
                    while(true) {
                        char c = sOff.findInLine(".").charAt(0);
                        if(c!=')')
                            temp3+=c;
                        else break;
                    }
                    temp3=(temp3.replaceAll(",", " ")).trim();
                    rsT = whatReg(temp3);
                    if(sOff.hasNext()) {
                        error();
                        return;
                    }
                    if(cons>=-32768 && cons<=32767 &&rsT>=0 && rsT<=31 &&rtT>=0 && rtT<=31)
                        dp.insMem.add(new Ins(temp, rsT, rtT,0,0,cons,""));
                    else {
                        error();
                        return;
                    }
                    if(lbl==true) {
                        lbl = false;
                        rem = 0;
                    }
                    break;
                case "sll":
                    rdT=whatReg(s.next()); rtT = whatReg(s.next()); int shift=s.nextInt(); 
                    if(shift>=0 && shift <=31 &&rdT>=0 && rdT<=31 &&rtT>=0 && rtT<=31)
                        dp.insMem.add(new Ins(temp, 0, rtT,rdT,shift,0,""));
                    else {
                        error();
                        return;
                    }
                    if(lbl==true) {
                        lbl = false;
                        rem = 0;
                    }
                    break;
                case "beq" : case "bne":
                    rsT=whatReg(s.next()); rtT = whatReg(s.next());
                    String rb = s.next();
                    if(rsT>=0 && rsT<=31 &&rtT>=0 && rtT<=31)
                        dp.insMem.add(new Ins(temp, rsT, rtT,0,0,0,rb));
                    else {
                        error();
                        return;
                    }
                    if(lbl==true) {
                        lbl = false;
                        rem = 0;
                    }
                    break;
                case "j" : case "jal":
                    String ja =s.next();
                    if(cons>=0 && cons<=67108863)
                        dp.insMem.add(new Ins(temp, 0, 0,0,0,0,ja));
                    else {
                        error();
                        return;
                    }
                    if(lbl==true) {
                        lbl = false;
                        rem = 0;
                    }
                    break;
                case "jr":
                    rsT=whatReg(s.next());
                    if(rsT>=0 && rsT<=31)
                        dp.insMem.add(new Ins(temp, rsT, 0,0,0,0,""));
                    else {
                        error();
                        return;
                    }
                    if(lbl==true) {
                        lbl = false;
                        rem = 0;
                    }
                    break;
                default:
                    if(lbl==false) {
                    String temp4="";
                    sTemp = new Scanner(st);
                    sTemp.useDelimiter("(,|\\s*\\s)+");
                    while(true) {
                        char c = sTemp.findInLine(".").charAt(0);
                        rem++;
                        if(c!=':')
                            temp4+=c;
                        else break;
                    }
                    temp4=temp4.trim();
                    for(int k=0; k<dp.lbls.size(); k++)
                        if(dp.lbls.get(k).name.equals(temp4)) {
                            error();
                            return;
                        }
                    dp.lbls.add(new Lbl(temp4, i));
                    i--;
                    lbl = true;
                    }
                    else {
                        error();
                        return;
                    }
                    break;
                    
            }
                    if(s.hasNext() && lbl == false) {
                        error();
                        return;
                    }
                    i++;
        }
        }
                catch(Exception e) {
                    error();
                    return;
                }
        for(int j=0; j<dp.insMem.size(); j++) {
            String s= dp.insMem.get(j).name;
            if(s.equalsIgnoreCase("beq") || s.equalsIgnoreCase("bne")||s.equalsIgnoreCase("j")||s.equalsIgnoreCase("jal")) {
                boolean found = false;
                String sT = dp.insMem.get(j).address;
                for(int m=0; m<dp.lbls.size(); m++)  
                    if(sT.equals(dp.lbls.get(m).name)) {
                        found = true;
                        break;
                    }
                if(found==false) {
                    error();
                    return;
                }
            }
        }
        try {
            dp.simProgram();
        }
        catch(InvalidAddressException e) {
            JOptionPane.showMessageDialog(null, "error due to invalid address in the program");
            return;
        }
        finally {
            newP=true;
        }
        writeInTables(dp.allV.size()-1);
    }
        public void error() {
            JOptionPane.showMessageDialog(null, "error in entered program");
        }
    public int whatReg(String s) {
        switch(s) {
            case "$0": case "$zero": return 0;
            case "$1": case "$at": return 1;
            case "$2": case "$v0": return 2;
            case "$3": case "$v1": return 3;
            case "$4": case "$a0": return 4;
            case "$5": case "$a1": return 5;
            case "$6": case "$a2": return 6;
            case "$7": case "$a3": return 7;
            case "$8": case "$t0": return 8;
            case "$9": case "$t1": return 9;
            case "$10": case "$t2": return 10;
            case "$11": case "$t3": return 11;
            case "$12": case "$t4": return 12;
            case "$13": case "$t5": return 13;
            case "$14": case "$t6": return 14;
            case "$15": case "$t7": return 15;
            case "$16": case "$s0": return 16;
            case "$17": case "$s1": return 17;
            case "$18": case "$s2": return 18;
            case "$19": case "$s3": return 19;
            case "$20": case "$s4": return 20;
            case "$21": case "$s5": return 21;
            case "$22": case "$s6": return 22;
            case "$23": case "$s7": return 23;
            case "$24": case "$t8": return 24;
            case "$25": case "$t9": return 25;
            case "$26": case "$k0": return 26;
            case "$27": case "$k1": return 27;
            case "$28": case "$gp": return 28;
            case "$29": case "$sp": return 29;
            case "$30": case "$fp": return 30;
            case "$31": case "$ra": return 31;
        }
        return 32;
    }
    /**
     * Creates new form Frame1
     */
    public Frame1() {
        initComponents();
    }
    public javax.swing.JTextArea getArea() {
        return this.jTextArea1;
    }
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        txtFirst = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        txtMemVal = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        txtMemLoc = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        jLabel5 = new javax.swing.JLabel();
        btnAdd = new javax.swing.JButton();
        btnRun = new javax.swing.JButton();
        btnReset = new javax.swing.JButton();
        jScrollPane3 = new javax.swing.JScrollPane();
        table = new javax.swing.JTable();
        jScrollPane2 = new javax.swing.JScrollPane();
        table2 = new javax.swing.JTable();
        txtClk = new javax.swing.JTextField();
        jButton1 = new javax.swing.JButton();
        jLabel6 = new javax.swing.JLabel();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        txtFirst.setName(""); // NOI18N

        jLabel1.setText("Address of first instruction:");

        txtMemVal.setName(""); // NOI18N

        jLabel2.setText("Values to load initially in the memory:");

        txtMemLoc.setName(""); // NOI18N

        jLabel3.setText("Value:");

        jLabel4.setText("Location:");

        jTextArea1.setColumns(20);
        jTextArea1.setRows(5);
        jScrollPane1.setViewportView(jTextArea1);

        jLabel5.setText("Program:");

        btnAdd.setText("Add");
        btnAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddActionPerformed(evt);
            }
        });

        btnRun.setText("Run");
        btnRun.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRunActionPerformed(evt);
            }
        });

        btnReset.setText("Free up memory");
        btnReset.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnResetActionPerformed(evt);
            }
        });

        table.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {"PC", "0"},
                {"PC + 4 in IF/ID", "0"},
                {"Instruction in IF/ID", "0"},
                {"RegDst in ID/EX", "0"},
                {"ALUOp in ID/EX", "0"},
                {"Function code in ID/EX", "0"},
                {"ALUSrc in ID/EX", "0"},
                {"jal in ID/EX", "0"},
                {"beq in ID/EX", "0"},
                {"bne in ID/EX", "0"},
                {"MemWrite in ID/EX", "0"},
                {"MemRead in ID/EX", "0"},
                {"RegWrite in IDEX", "0"},
                {"PC + 4 in ID/EX", "0"},
                {"Read data 1 in ID/EX", "0"},
                {"Read data 2 in ID/EX", "0"},
                {"Sign extended in ID/EX", "0"},
                {"RT reg number in ID/EX", "0"},
                {"RD reg number in ID/EX", "0"},
                {"shamt in ID/EX", "0"},
                {"MemToReg in ID/EX", "0"},
                {"Unsigned in ID/EX", "0"},
                {"RegWrite in EX/MEM", "0"},
                {"MemRead in EX/MEM", "0"},
                {"PC + 4 in EX/MEM", "0"},
                {"MemWrite in EX/MEM", "0"},
                {"ALU result int EX/MEM", "0"},
                {"Read data 2 in EX/MEM", "0"},
                {"WriteRegMux in EX/MEM", "0"},
                {"MemToReg in EX/MEM", "0"},
                {"jal in EX/MEM", "0"},
                {"WriteRegMux2 in EX/MEM", "0"},
                {"RegWrite in MEM/WB", "0"},
                {"Read data in MEM/WB", "0"},
                {"PC + 4 in MEM/WB", "0"},
                {"ALU result in MEM/WB", "0"},
                {"WriteRegMux in MEM/WB", "0"},
                {"MemToReg in MEM/WB", "0"},
                {"jal in MEM/WB", "0"},
                {"WriteRegMux2 in MEM/WB", "0"},
                {"PC + 4", "0"},
                {"Instruction", "0"},
                {"RS register number", "0"},
                {"OP code in ID stage", "0"},
                {"RT register number", "0"},
                {"RD register number", "0"},
                {"Immediate constant", "0"},
                {"Sign extended constant", "0"},
                {"Read data 1", "0"},
                {"Read data 2", "0"},
                {"shamt", "0"},
                {"RegDst", "0"},
                {"jal", "0"},
                {"beq", "0"},
                {"bne", "0"},
                {"MemRead", "0"},
                {"MemToReg", "0"},
                {"ALUOp", "0"},
                {"MemWrite", "0"},
                {"ALUSrc", "0"},
                {"RegWrite", "0"},
                {"Shifted constant", "0"},
                {"Second adder", "0"},
                {"WriteRegMux", "0"},
                {"ALU Control", "0"},
                {"ReadMux", "0"},
                {"ALU result", "0"},
                {"Zero flag", "0"},
                {"PCSrc", "0"},
                {"Read data", "No value"},
                {"WBMux", "0"},
                {"26-bit address", "0"},
                {"Concatenated address", "0"},
                {"BranchMux", "0"},
                {"Function code", "0"},
                {"Inverted zero flag", "0"},
                {"Unsigned", "0"},
                {"JumpMux", "0"},
                {"jrMux", "0"},
                {"WriteRegMux2", "0"},
                {"WBMux2", "0"},
                {"jORjal", "0"},
                {"jr", "0"},
                {"Shifted jump address", "0"},
                {"OP code in IF stage", "0"},
                {"PC + 4 first 4 bits", "0"}
            },
            new String [] {
                "Name", "Value"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane3.setViewportView(table);
        if (table.getColumnModel().getColumnCount() > 0) {
            table.getColumnModel().getColumn(0).setResizable(false);
            table.getColumnModel().getColumn(1).setResizable(false);
        }

        table2.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {"$zero ($0)", "0"},
                {"$at ($1)", "0"},
                {"$v0 ($2)", "0"},
                {"$v1 ($3)", "0"},
                {"$a0 ($4)", "0"},
                {"$a1 ($5)", "0"},
                {"$a2 ($6)", "0"},
                {"$a3 ($7)", "0"},
                {"$t0 ($8)", "0"},
                {"$t1 ($9)", "0"},
                {"$t2 ($10)", "0"},
                {"$t3 ($11)", "0"},
                {"$t4 ($12)", "0"},
                {"$t5 ($13)", "0"},
                {"$t6 ($14)", "0"},
                {"$t7 ($15)", "0"},
                {"$s0 ($16)", "0"},
                {"$s1 ($17)", "0"},
                {"$s2 ($18)", "0"},
                {"$s3 ($19)", "0"},
                {"$s4 ($20)", "0"},
                {"$s5 ($21)", "0"},
                {"$s6 ($22)", "0"},
                {"$s7 ($23)", "0"},
                {"$t8 ($24)", "0"},
                {"$t9 ($25)", "0"},
                {"$k0 ($26)", "0"},
                {"$k1 ($27)", "0"},
                {"$gp ($28)", "0"},
                {"$sp (29)", "2000000000"},
                {"$fp (30)", "0"},
                {"$ra (31)", "0"}
            },
            new String [] {
                "Register", "Value"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane2.setViewportView(table2);
        if (table2.getColumnModel().getColumnCount() > 0) {
            table2.getColumnModel().getColumn(0).setResizable(false);
            table2.getColumnModel().getColumn(1).setResizable(false);
        }

        jButton1.setText("ok");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jLabel6.setText("Go to clock cycle:");

        jButton2.setText("next clock cycle");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jButton3.setText("previous clock cycle");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        jButton4.setText("View your used memory values");
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel4)
                        .addGap(4, 4, 4)
                        .addComponent(txtMemLoc, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(10, 10, 10)
                        .addComponent(btnReset))
                    .addComponent(jLabel5)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addGap(18, 18, 18)
                        .addComponent(txtMemVal, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(10, 10, 10)
                        .addComponent(btnAdd))
                    .addComponent(jLabel1)
                    .addComponent(txtFirst, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 221, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(6, 6, 6)
                        .addComponent(btnRun)))
                .addGap(49, 49, 49)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 392, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 328, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(188, 188, 188)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jLabel6)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jButton3)
                                        .addGap(18, 18, 18)
                                        .addComponent(txtClk, javax.swing.GroupLayout.PREFERRED_SIZE, 104, javax.swing.GroupLayout.PREFERRED_SIZE))))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addComponent(jButton4)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(19, 19, 19)))
                        .addGap(16, 16, 16)
                        .addComponent(jButton2))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(8, 8, 8)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addGap(7, 7, 7)
                        .addComponent(txtFirst, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(13, 13, 13)
                        .addComponent(jLabel2)
                        .addGap(5, 5, 5)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(3, 3, 3)
                                .addComponent(jLabel3))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(1, 1, 1)
                                .addComponent(txtMemVal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(btnAdd))
                        .addGap(3, 3, 3)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(3, 3, 3)
                                .addComponent(jLabel4))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(1, 1, 1)
                                .addComponent(txtMemLoc, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(btnReset))
                        .addGap(16, 16, 16)
                        .addComponent(jLabel5)
                        .addGap(6, 6, 6)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 484, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(461, 461, 461)
                                .addComponent(btnRun))))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 538, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 538, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(34, 34, 34)
                                .addComponent(jLabel6)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(txtClk, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jButton3))
                                    .addComponent(jButton2))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButton1))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButton4)))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnRunActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRunActionPerformed
        if(newP==true) {
            dm.clear();
            newP=false;
        }
        dp = new DataPath();
        dp.dataMem=dm;
        for(int i=0; i<86; i++) {
                if(i==69) {
                    table.setValueAt("No value", i, 1);
                    continue;
                }
                table.setValueAt(0, i, 1);
            }
            for(int i=0; i<32; i++) {
                if(i==29) {
                    table2.setValueAt(2000000000, i, 1);
                    continue;
                }
                table2.setValueAt(0, i, 1);
            }
        cycle=0;
        txtClk.setText("");
        int first = 0;
        try {first = Integer.parseInt(txtFirst.getText().trim());}
        catch(Exception e) {
            JOptionPane.showMessageDialog(null, "invalid number for starting pc");
            return;
        }
        if(first < 0 || first%4!=0) {
            JOptionPane.showMessageDialog(null, "invalid address for starting pc");
            return;
        }
        dp.jrMux=dp.firstPc=first;
        takeProgram();
    }//GEN-LAST:event_btnRunActionPerformed

    private void btnAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddActionPerformed

        int val = 0, loc=0;
        try {
            val = Integer.parseInt(txtMemVal.getText().trim());
            loc = Integer.parseInt(txtMemLoc.getText().trim());
        }
        catch(Exception e) {
            JOptionPane.showMessageDialog(null, "error in input");
            return;
        }
        if(loc<0 || loc%4!=0) {
            JOptionPane.showMessageDialog(null, "invalid memory address");
            return;
        }
        if(newP==true) {
            dm.clear();
            newP=false;
        }
        boolean found = false;
        for(int i=0; i<dm.size(); i++)
            if((dm.get(i)).location==loc) {
               (dm.get(i)).val = val;
               found = true;
               break;
            }
        if(found==false)
            dm.add(new DataMemElement(loc,val));
    }//GEN-LAST:event_btnAddActionPerformed

    private void btnResetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnResetActionPerformed
        dm.clear();
    }//GEN-LAST:event_btnResetActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        int i=0;
        try {
            i=Integer.parseInt(txtClk.getText());
        }
        catch(Exception e) {
            JOptionPane.showMessageDialog(null,"invalid number");
            return;
        }
        if(dp!=null) {
            if(dp.allV.size()>0 && i>=1 && i<=dp.allV.size())
                writeInTables(i-1);
            else
                JOptionPane.showMessageDialog(null,"invalid cycle");
        }
        else
            JOptionPane.showMessageDialog(null,"no program present");
        
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        if(cycle > 1) {
            txtClk.setText((cycle-1)+"");
            writeInTables(cycle-2);
        }
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        if(cycle < dp.allV.size()) {
            txtClk.setText((cycle+1)+"");
            writeInTables(cycle);
        }
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        MemV mv = new MemV();
        mv.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        for(int i=0; i<dm.size(); i++)
            mv.jTextArea1.setText(mv.jTextArea1.getText()+"Location: "+dm.get(i).location+", Value: "+dm.get(i).val+"\n");
        mv.setVisible(true);
    }//GEN-LAST:event_jButton4ActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Frame1.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Frame1.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Frame1.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Frame1.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Frame1().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAdd;
    private javax.swing.JButton btnReset;
    private javax.swing.JButton btnRun;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JTable table;
    private javax.swing.JTable table2;
    private javax.swing.JTextField txtClk;
    private javax.swing.JTextField txtFirst;
    private javax.swing.JTextField txtMemLoc;
    private javax.swing.JTextField txtMemVal;
    // End of variables declaration//GEN-END:variables
}
