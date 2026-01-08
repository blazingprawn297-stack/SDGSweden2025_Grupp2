package SDGSweden2025;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
import oru.inf.InfDB;
import oru.inf.InfException;

import javax.swing.JOptionPane;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author chris
 */
public class ÄndraProjektUppgifter extends javax.swing.JFrame {

    private InfDB idb;
    private String epost;
    private boolean isAdmin;

    // för att kunna slå upp id när man väljer i comboboxar
    private HashMap<String, String> projektMap = new HashMap<>(); // "pid - namn" -> pid
    private HashMap<String, String> landMap = new HashMap<>();

    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(ÄndraProjektUppgifter.class.getName());

    /**
     * Creates new form ÄndraProjektUppgifter
     */
    public ÄndraProjektUppgifter() {
        initComponents();
    }

    public ÄndraProjektUppgifter(InfDB idb, String epost, boolean isAdmin) {
        this.idb = idb;
        this.epost = epost;
        this.isAdmin = isAdmin;

        initComponents();
        setLocationRelativeTo(null);

        // Fyll comboboxar
        initStatusPrioritet();
        loadLand();
        loadMinaProjektSomChef();

        // Rensa fält tills projekt är valt
        clearFields();
    }

    private void initStatusPrioritet() {
        jComboBox2.removeAllItems(); // status
        jComboBox2.addItem("Planerat");
        jComboBox2.addItem("Pågående");
        jComboBox2.addItem("Avslutat");

        jComboBox3.removeAllItems(); // prioritet
        jComboBox3.addItem("Låg");
        jComboBox3.addItem("Medel");
        jComboBox3.addItem("Hög");
    }

    private void loadLand() {
        landMap.clear();
        jComboBox4.removeAllItems();
        jComboBox4.addItem("Välj land");

        try {
            // Antagande (vanligt i SDG-projekt): land(lid, namn)
            ArrayList<HashMap<String, String>> rows
                    = idb.fetchRows("SELECT lid, namn FROM land ORDER BY namn");

            if (rows != null) {
                for (HashMap<String, String> r : rows) {
                    String lid = r.get("lid");
                    String namn = r.get("namn");
                    String key = lid + " - " + namn;
                    landMap.put(key, lid);
                    jComboBox4.addItem(key);
                }
            }
        } catch (Exception e) {
            // Om er land-tabell heter/ser annorlunda ut så får du ändra SQL här.
            JOptionPane.showMessageDialog(this,
                    "Kunde inte ladda land-lista.\nKontrollera tabellen 'land'.\n" + e.getMessage());
        }
    }

    private void loadMinaProjektSomChef() {
        projektMap.clear();
        jComboBox1.removeAllItems();
        jComboBox1.addItem("Välj projekt");

        try {
            String aid = idb.fetchSingle("SELECT aid FROM anstalld WHERE epost = '" + epost + "'");
            if (aid == null) {
                JOptionPane.showMessageDialog(this, "Kunde inte hitta anställd (aid) för " + epost);
                return;
            }

            ArrayList<HashMap<String, String>> rows = idb.fetchRows(
                    "SELECT pid, projektnamn FROM projekt WHERE projektchef = " + aid + " ORDER BY projektnamn"
            );

            if (rows != null) {
                for (HashMap<String, String> r : rows) {
                    String pid = r.get("pid");
                    String namn = r.get("projektnamn");
                    String key = pid + " - " + namn;
                    projektMap.put(key, pid);
                    jComboBox1.addItem(key);
                }
            }

        } catch (InfException e) {
            JOptionPane.showMessageDialog(this, "Databasfel: " + e.getMessage());
        }
    }

    private void clearFields() {
        jTextField1.setText("");
        jTextField2.setText("");
        jTextField3.setText("");
        jTextField4.setText("");
        jTextField5.setText("");

        // sätt gärna default-val
        if (jComboBox2.getItemCount() > 0) {
            jComboBox2.setSelectedIndex(0);
        }
        if (jComboBox3.getItemCount() > 0) {
            jComboBox3.setSelectedIndex(0);
        }
        if (jComboBox4.getItemCount() > 0) {
            jComboBox4.setSelectedIndex(0);
        }
    }

    // ====== HÄMTA + VISA VALT PROJEKT ======
    private void loadSelectedProjectToFields() {
        String vald = (String) jComboBox1.getSelectedItem();
        if (vald == null || vald.equals("Välj projekt")) {
            clearFields();
            return;
        }

        String pid = projektMap.get(vald);
        if (pid == null) {
            clearFields();
            return;
        }

        try {
            HashMap<String, String> row = idb.fetchRow(
                    "SELECT projektnamn, beskrivning, startdatum, slutdatum, kostnad, status, prioritet, land "
                    + "FROM projekt WHERE pid = " + pid
            );

            if (row == null) {
                JOptionPane.showMessageDialog(this, "Kunde inte läsa projektet.");
                return;
            }

            jTextField1.setText(nvl(row.get("projektnamn")));
            jTextField2.setText(nvl(row.get("beskrivning")));
            jTextField3.setText(nvl(row.get("startdatum")));
            jTextField4.setText(nvl(row.get("slutdatum")));
            jTextField5.setText(nvl(row.get("kostnad")));

            // Status / prioritet
            setComboToValue(jComboBox2, row.get("status"));
            setComboToValue(jComboBox3, row.get("prioritet"));

            // Land: vi antar att projekt.land är ett ID (lid)
            String landId = row.get("land");
            selectLandById(landId);

        } catch (InfException e) {
            JOptionPane.showMessageDialog(this, "Databasfel: " + e.getMessage());
        }
    }

    private void setComboToValue(javax.swing.JComboBox<String> combo, String value) {
        if (value == null) {
            return;
        }
        for (int i = 0; i < combo.getItemCount(); i++) {
            if (value.equalsIgnoreCase(combo.getItemAt(i))) {
                combo.setSelectedIndex(i);
                return;
            }
        }
    }

    private void selectLandById(String landId) {
        if (landId == null) {
            jComboBox4.setSelectedIndex(0);
            return;
        }
        for (int i = 0; i < jComboBox4.getItemCount(); i++) {
            String item = jComboBox4.getItemAt(i);
            if (item != null && item.startsWith(landId.trim() + " -")) {
                jComboBox4.setSelectedIndex(i);
                return;
            }
        }
        // om ej hittad
        jComboBox4.setSelectedIndex(0);
    }

    private String nvl(String s) {
        return s == null ? "" : s;
    }

    // ====== SPARA ======
    private void sparaProjekt() {
        String vald = (String) jComboBox1.getSelectedItem();
        if (vald == null || vald.equals("Välj projekt")) {
            JOptionPane.showMessageDialog(this, "Välj ett projekt först.");
            return;
        }

        String pid = projektMap.get(vald);
        if (pid == null) {
            JOptionPane.showMessageDialog(this, "Fel: kunde inte läsa PID från valet.");
            return;
        }

        String projektnamn = jTextField1.getText().trim();
        String beskrivning = jTextField2.getText().trim();
        String startdatum = jTextField3.getText().trim();
        String slutdatum = jTextField4.getText().trim();
        String kostnad = jTextField5.getText().trim();

        String status = (String) jComboBox2.getSelectedItem();
        String prioritet = (String) jComboBox3.getSelectedItem();
        String landVal = (String) jComboBox4.getSelectedItem();

        if (projektnamn.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Projektnamn får inte vara tomt.");
            return;
        }

        // enkel validering på datumformat (YYYY-MM-DD)
        if (!startdatum.isEmpty() && !startdatum.matches("\\d{4}-\\d{2}-\\d{2}")) {
            JOptionPane.showMessageDialog(this, "Startdatum måste vara i formatet YYYY-MM-DD.");
            return;
        }
        if (!slutdatum.isEmpty() && !slutdatum.matches("\\d{4}-\\d{2}-\\d{2}")) {
            JOptionPane.showMessageDialog(this, "Slutdatum måste vara i formatet YYYY-MM-DD.");
            return;
        }

        // kostnad: tillåt heltal eller decimal
        if (!kostnad.isEmpty() && !kostnad.matches("\\d+(\\.\\d+)?")) {
            JOptionPane.showMessageDialog(this, "Kostnad måste vara ett tal (t.ex. 10000 eller 10000.50).");
            return;
        }

        if (landVal == null || landVal.equals("Välj land")) {
            JOptionPane.showMessageDialog(this, "Välj ett land.");
            return;
        }

        String landId = landMap.get(landVal);
        if (landId == null) {
            // fallback: om combobox-item börjar med id
            landId = landVal.split(" - ")[0].trim();
        }

        try {
            // Säkerhet: dubbelkolla att användaren verkligen är projektchef för detta projekt
            String aid = idb.fetchSingle("SELECT aid FROM anstalld WHERE epost = '" + epost + "'");
            String ok = idb.fetchSingle("SELECT pid FROM projekt WHERE pid = " + pid + " AND projektchef = " + aid);
            if (ok == null) {
                JOptionPane.showMessageDialog(this, "Du är inte projektchef för detta projekt.");
                return;
            }

            String sql
                    = "UPDATE projekt SET "
                    + "projektnamn = '" + esc(projektnamn) + "', "
                    + "beskrivning = '" + esc(beskrivning) + "', "
                    + "startdatum = " + (startdatum.isEmpty() ? "NULL" : ("'" + esc(startdatum) + "'")) + ", "
                    + "slutdatum = " + (slutdatum.isEmpty() ? "NULL" : ("'" + esc(slutdatum) + "'")) + ", "
                    + "kostnad = " + (kostnad.isEmpty() ? "NULL" : kostnad) + ", "
                    + "status = '" + esc(status) + "', "
                    + "prioritet = '" + esc(prioritet) + "', "
                    + "land = " + landId + " "
                    + "WHERE pid = " + pid;

            idb.update(sql);

            JOptionPane.showMessageDialog(this, "Projektet uppdaterades!");
            // uppdatera projektlistan (om du ändrade namn)
            loadMinaProjektSomChef();

            // försök återvälja samma pid
            selectProjectByPid(pid);

        } catch (InfException e) {
            JOptionPane.showMessageDialog(this, "Databasfel vid sparning: " + e.getMessage());
        }
    }

    private void selectProjectByPid(String pid) {
        for (int i = 0; i < jComboBox1.getItemCount(); i++) {
            String item = jComboBox1.getItemAt(i);
            if (item != null && item.startsWith(pid + " -")) {
                jComboBox1.setSelectedIndex(i);
                return;
            }
        }
        jComboBox1.setSelectedIndex(0);
    }

    private String esc(String s) {
        return s == null ? "" : s.replace("'", "''");
    }

    // ====== NAVIGATION ======
    private void gaTillbaka() {
        this.dispose();
        new Meny(idb, epost, isAdmin).setVisible(true);
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
        jLabel1 = new javax.swing.JLabel();
        jComboBox1 = new javax.swing.JComboBox<>();
        jLabel2 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jTextField2 = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jTextField3 = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        jTextField4 = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        jTextField5 = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        jComboBox2 = new javax.swing.JComboBox<>();
        jLabel8 = new javax.swing.JLabel();
        jComboBox3 = new javax.swing.JComboBox<>();
        jLabel9 = new javax.swing.JLabel();
        jComboBox4 = new javax.swing.JComboBox<>();
        btnSpara = new javax.swing.JButton();
        btnTillbaka = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel1.setText("Välj Projekt:");

        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jComboBox1.addActionListener(this::jComboBox1ActionPerformed);

        jLabel2.setText("Projektnamn:");

        jTextField1.setText("jTextField1");
        jTextField1.addActionListener(this::jTextField1ActionPerformed);

        jLabel3.setText("Beskrivning:");

        jTextField2.setText("jTextField2");
        jTextField2.addActionListener(this::jTextField2ActionPerformed);

        jLabel4.setText("Startdatum:");

        jTextField3.setText("jTextField3");
        jTextField3.addActionListener(this::jTextField3ActionPerformed);

        jLabel5.setText("Slutdatum:");

        jTextField4.setText("jTextField4");
        jTextField4.addActionListener(this::jTextField4ActionPerformed);

        jLabel6.setText("Kostnad:");

        jTextField5.setText("jTextField5");
        jTextField5.addActionListener(this::jTextField5ActionPerformed);

        jLabel7.setText("Status:");

        jComboBox2.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jComboBox2.addActionListener(this::jComboBox2ActionPerformed);

        jLabel8.setText("Prioritet:");

        jComboBox3.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jComboBox3.addActionListener(this::jComboBox3ActionPerformed);

        jLabel9.setText("Land:");

        jComboBox4.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jComboBox4.addActionListener(this::jComboBox4ActionPerformed);

        btnSpara.setText("Spara");

        btnTillbaka.setText("Tillbaka");
        btnTillbaka.addActionListener(this::btnTillbakaActionPerformed);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(87, 87, 87)
                .addComponent(btnSpara, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnTillbaka, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jSeparator1)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2)
                            .addComponent(jLabel3))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jTextField1)
                            .addComponent(jTextField2)))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, 264, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel5)
                                    .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 58, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                        .addComponent(jLabel9, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(jLabel8, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(jComboBox2, 0, 146, Short.MAX_VALUE)
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                        .addComponent(jTextField5, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 120, Short.MAX_VALUE)
                                        .addComponent(jTextField4, javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jTextField3, javax.swing.GroupLayout.Alignment.LEADING))
                                    .addComponent(jComboBox3, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jComboBox4, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addGap(40, 40, 40))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(40, 40, 40)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(13, 13, 13)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3))
                .addGap(10, 10, 10)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(jTextField4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(jTextField5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(9, 9, 9)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jComboBox2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel7))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(jComboBox3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9)
                    .addComponent(jComboBox4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(28, 28, 28)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnSpara)
                    .addComponent(btnTillbaka))
                .addContainerGap(29, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnTillbakaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTillbakaActionPerformed
        gaTillbaka();
    }//GEN-LAST:event_btnTillbakaActionPerformed

    private void jComboBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox1ActionPerformed
        loadSelectedProjectToFields();
    }//GEN-LAST:event_jComboBox1ActionPerformed

    private void jTextField1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField1ActionPerformed

    private void jTextField2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField2ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField2ActionPerformed

    private void jTextField3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField3ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField3ActionPerformed

    private void jTextField4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField4ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField4ActionPerformed

    private void jTextField5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField5ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField5ActionPerformed

    private void jComboBox2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox2ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jComboBox2ActionPerformed

    private void jComboBox3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox3ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jComboBox3ActionPerformed

    private void jComboBox4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox4ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jComboBox4ActionPerformed

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
        java.awt.EventQueue.invokeLater(() -> new ÄndraProjektUppgifter().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnSpara;
    private javax.swing.JButton btnTillbaka;
    private javax.swing.JComboBox<String> jComboBox1;
    private javax.swing.JComboBox<String> jComboBox2;
    private javax.swing.JComboBox<String> jComboBox3;
    private javax.swing.JComboBox<String> jComboBox4;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField2;
    private javax.swing.JTextField jTextField3;
    private javax.swing.JTextField jTextField4;
    private javax.swing.JTextField jTextField5;
    // End of variables declaration//GEN-END:variables
}
