package SDGSweden2025;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
import oru.inf.InfDB;
import oru.inf.InfException;

import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author chris
 */
public class StatistikProjektchef extends javax.swing.JFrame {

    private InfDB idb;
    private String epost;
    private boolean isAdmin;

    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(StatistikProjektchef.class.getName());

    /**
     * Creates new form StatistikProjektchef
     */
    public StatistikProjektchef() {
        initComponents();
    }

    public StatistikProjektchef(InfDB idb, String epost, boolean isAdmin) {
        this.idb = idb;
        this.epost = epost;
        this.isAdmin = isAdmin;

        initComponents();
        setLocationRelativeTo(null);

        // Snygga startvärden för statistik-raderna
        jLabel6.setText("-");
        jLabel7.setText("-");
        jLabel8.setText("-");

        // Initiera tabellerna (kolumner)
        initTableMinaProjekt();
        initTableLandProjekt();

        // Ladda data
        loadMinaProjektStatistik();
        loadLanderTillCombo();

        // Koppla knappar (om du redan kopplat i Design gör det inget)
        jButton1.addActionListener(evt -> visaStatistikLand());
        jButton2.addActionListener(evt -> gaTillbaka());
    }

    // -------------------------
    // TABELLER (KOLUMNER)
    // -------------------------
    private void initTableMinaProjekt() {
        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("Pid");
        model.addColumn("Projektnamn");
        model.addColumn("Kostnad");
        model.addColumn("Status");
        model.addColumn("Startdatum");
        model.addColumn("Slutdatum");
        model.addColumn("Land");

        jTable1.setModel(model);

        // Behåll scroll + många kolumner
        jTable1.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
    }

    private void initTableLandProjekt() {
        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("Pid");
        model.addColumn("Projektnamn");
        model.addColumn("Kostnad");
        model.addColumn("Status");
        model.addColumn("Startdatum");
        model.addColumn("Slutdatum");
        model.addColumn("Projektchef");

        jTable2.setModel(model);

        jTable2.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
    }

    // -------------------------
    // 1) STATISTIK: MINA PROJEKT (projektchef)
    // -------------------------
    private void loadMinaProjektStatistik() {
        DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
        model.setRowCount(0);

        try {
            // Hämta min aid via epost
            String aid = idb.fetchSingle("SELECT aid FROM anstalld WHERE epost = '" + epost + "'");
            if (aid == null) {
                JOptionPane.showMessageDialog(this, "Kunde inte hitta anställd för epost: " + epost);
                return;
            }

            // Hämta alla projekt där jag är projektchef
            String sql
                    = "SELECT pid, projektnamn, kostnad, status, startdatum, slutdatum, land "
                    + "FROM projekt "
                    + "WHERE projektchef = " + aid + " "
                    + "ORDER BY pid";

            ArrayList<HashMap<String, String>> rows = idb.fetchRows(sql);

            int antal = 0;
            double total = 0.0;

            if (rows != null) {
                for (HashMap<String, String> r : rows) {
                    antal++;

                    String kostnadStr = r.get("kostnad");
                    double kostnad = parseDoubleSafe(kostnadStr);
                    total += kostnad;

                    model.addRow(new Object[]{
                        r.get("pid"),
                        r.get("projektnamn"),
                        kostnadStr,
                        r.get("status"),
                        r.get("startdatum"),
                        r.get("slutdatum"),
                        r.get("land")
                    });
                }
            }

            double medel = (antal > 0) ? (total / antal) : 0.0;

            // Visa statistik i dina labels (jLabel6-8)
            jLabel6.setText(String.valueOf(antal));
            jLabel7.setText(formatMoney(total) + " kr");
            jLabel8.setText(formatMoney(medel) + " kr");

        } catch (InfException e) {
            JOptionPane.showMessageDialog(this, "Databasfel (mina projekt): " + e.getMessage());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Fel (mina projekt): " + e.getMessage());
        }
    }

    // -------------------------
    // 2) STATISTIK: KOSTNADER PER LAND
    // -------------------------
    private void loadLanderTillCombo() {
        try {
            jComboBox1.removeAllItems();
            jComboBox1.addItem("Välj land");

            // Försök: tabellen land(lid, namn)
            ArrayList<HashMap<String, String>> rows
                    = idb.fetchRows("SELECT lid, namn FROM land ORDER BY namn");

            if (rows != null) {
                for (HashMap<String, String> r : rows) {
                    jComboBox1.addItem(r.get("lid") + " - " + r.get("namn"));
                }
            }

        } catch (Exception e) {
            // Fallback: om land-tabellen inte finns eller heter annat
            try {
                jComboBox1.removeAllItems();
                jComboBox1.addItem("Välj land");

                ArrayList<HashMap<String, String>> rows2
                        = idb.fetchRows("SELECT DISTINCT land FROM projekt ORDER BY land");

                if (rows2 != null) {
                    for (HashMap<String, String> r : rows2) {
                        String landId = r.get("land");
                        jComboBox1.addItem(landId + " - Land " + landId);
                    }
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Kunde inte ladda länder: " + ex.getMessage());
            }
        }
    }

    private void visaStatistikLand() {
        String vald = (String) jComboBox1.getSelectedItem();

        if (vald == null || vald.equals("Välj land")) {
            JOptionPane.showMessageDialog(this, "Välj ett land först.");
            return;
        }

        String landId = vald.split(" - ")[0].trim();
        if (!landId.matches("\\d+")) {
            JOptionPane.showMessageDialog(this, "Ogiltigt land-id.");
            return;
        }

        loadProjektForLand(Integer.parseInt(landId));
    }

    private void loadProjektForLand(int landId) {
        DefaultTableModel model = (DefaultTableModel) jTable2.getModel();
        model.setRowCount(0);

        try {
            String sql
                    = "SELECT pid, projektnamn, kostnad, status, startdatum, slutdatum, projektchef "
                    + "FROM projekt "
                    + "WHERE land = " + landId + " "
                    + "ORDER BY pid";

            ArrayList<HashMap<String, String>> rows = idb.fetchRows(sql);

            int antal = 0;
            double total = 0.0;

            if (rows != null) {
                for (HashMap<String, String> r : rows) {
                    antal++;

                    String kostnadStr = r.get("kostnad");
                    double kostnad = parseDoubleSafe(kostnadStr);
                    total += kostnad;

                    model.addRow(new Object[]{
                        r.get("pid"),
                        r.get("projektnamn"),
                        kostnadStr,
                        r.get("status"),
                        r.get("startdatum"),
                        r.get("slutdatum"),
                        r.get("projektchef")
                    });
                }
            }

            // En enkel "summeringsrad" längst ner (valfritt men tydligt)
            if (antal > 0) {
                model.addRow(new Object[]{
                    "",
                    "SUMMA (" + antal + " projekt)",
                    formatMoney(total) + " kr",
                    "",
                    "",
                    "",
                    ""
                });
            }

        } catch (InfException e) {
            JOptionPane.showMessageDialog(this, "Databasfel (land): " + e.getMessage());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Fel (land): " + e.getMessage());
        }
    }

    // -------------------------
    // NAVIGATION
    // -------------------------
    private void gaTillbaka() {
        this.dispose();
        new Meny(idb, epost, isAdmin).setVisible(true);
    }

    // -------------------------
    // HJÄLPMETODER
    // -------------------------
    private double parseDoubleSafe(String s) {
        if (s == null) {
            return 0.0;
        }
        try {
            return Double.parseDouble(s.replace(",", ".").trim());
        } catch (Exception e) {
            return 0.0;
        }
    }

    private String formatMoney(double value) {
        // Grundnivå: inga avancerade formatteringar
        // (du kan byta till DecimalFormat om du vill)
        return String.format(java.util.Locale.US, "%.2f", value);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jLabel1 = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jSeparator2 = new javax.swing.JSeparator();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTable2 = new javax.swing.JTable();
        jLabel5 = new javax.swing.JLabel();
        jComboBox1 = new javax.swing.JComboBox<>();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jTable1.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        jScrollPane1.setViewportView(jTable1);

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 15)); // NOI18N
        jLabel1.setText("Statistik: Mina projekt (projektchef)");

        jLabel2.setText("Antal projekt:");

        jLabel3.setText("Total kostnad:");

        jLabel4.setText("Medelkostnad:");

        jTable2.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane2.setViewportView(jTable2);

        jLabel5.setFont(new java.awt.Font("Segoe UI", 1, 15)); // NOI18N
        jLabel5.setText("Statistik: Kostnader per land");

        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jComboBox1.addActionListener(this::jComboBox1ActionPerformed);

        jButton1.setText("Visa");
        jButton1.addActionListener(this::jButton1ActionPerformed);

        jButton2.setText("Tillbaka");
        jButton2.addActionListener(this::jButton2ActionPerformed);

        jLabel6.setText("jLabel6");

        jLabel7.setText("jLabel7");

        jLabel8.setText("jLabel8");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 266, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(106, 106, 106))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jButton1))
                            .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 286, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(66, 66, 66))))
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(170, 170, 170)
                        .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 129, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(17, 17, 17)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(16, 16, 16)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jSeparator1, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jSeparator2, javax.swing.GroupLayout.Alignment.TRAILING))
                .addContainerGap())
            .addGroup(layout.createSequentialGroup()
                .addGap(36, 36, 36)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel6)
                .addGap(18, 18, 18)
                .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 86, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel8, javax.swing.GroupLayout.DEFAULT_SIZE, 44, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(jLabel1)
                .addGap(8, 8, 8)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 164, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jLabel3)
                    .addComponent(jLabel4)
                    .addComponent(jLabel6)
                    .addComponent(jLabel7)
                    .addComponent(jLabel8))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel5)
                .addGap(11, 11, 11)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jButton2)
                .addContainerGap(18, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jComboBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jComboBox1ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        visaStatistikLand();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        gaTillbaka();
    }//GEN-LAST:event_jButton2ActionPerformed

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
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> new StatistikProjektchef().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JComboBox<String> jComboBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JTable jTable1;
    private javax.swing.JTable jTable2;
    // End of variables declaration//GEN-END:variables
}
