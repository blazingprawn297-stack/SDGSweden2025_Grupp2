package SDGSweden2025;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
import oru.inf.InfDB;
import oru.inf.InfException;

import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author chris
 */
public class HanteraHandläggare extends javax.swing.JFrame {

    private InfDB idb;
    private String epost;
    private boolean isAdmin; // används bara för att skicka vidare, inte för behörighet här
    private int pid;

    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(HanteraHandläggare.class.getName());

    /**
     * Creates new form HanteraHandläggare
     */
    public HanteraHandläggare() {
        initComponents();
    }

    public HanteraHandläggare(InfDB idb, String epost, boolean isAdmin, int pid) {
        this.idb = idb;
        this.epost = epost;
        this.isAdmin = isAdmin;
        this.pid = pid;

        initComponents();
        setLocationRelativeTo(null);

        // Behörighetskontroll: endast projektchef får öppna/hantera
        if (!arProjektchefForPid()) {
            JOptionPane.showMessageDialog(this,
                    "Du är inte projektchef för detta projekt och får inte hantera handläggare.");
            dispose();
            return;
        }
        loadAllaHandlaggare();
        loadHandlaggareIProjekt();
    }

    private boolean arProjektchefForPid() {
        try {
            String aid = idb.fetchSingle("SELECT aid FROM anstalld WHERE epost = '" + epost + "'");
            if (aid == null) {
                return false;
            }

            String chefAid = idb.fetchSingle("SELECT projektchef FROM projekt WHERE pid = " + pid);
            if (chefAid == null) {
                return false;
            }

            return aid.trim().equals(chefAid.trim());
        } catch (Exception e) {
            return false;
        }
    }

    private void loadAllaHandlaggare() {
        try {
            cmbAllaHandlaggare.removeAllItems();
            cmbAllaHandlaggare.addItem("Välj handläggare");

            String sql
                    = "SELECT a.aid, a.fornamn, a.efternamn, a.epost "
                    + "FROM anstalld a "
                    + "JOIN handlaggare h ON a.aid = h.aid "
                    + "ORDER BY a.fornamn, a.efternamn";

            ArrayList<HashMap<String, String>> rows = idb.fetchRows(sql);

            if (rows != null) {
                for (HashMap<String, String> r : rows) {
                    String aid = r.get("aid");
                    String namn = r.get("fornamn") + " " + r.get("efternamn");
                    String mail = r.get("epost");
                    cmbAllaHandlaggare.addItem(aid + " - " + namn + " (" + mail + ")");
                }
            }

        } catch (InfException e) {
            JOptionPane.showMessageDialog(this, "Kunde inte hämta handläggare: " + e.getMessage());
        }
    }

    private void loadHandlaggareIProjekt() {
        DefaultListModel<String> model = new DefaultListModel<>();

        try {
            String sql
                    = "SELECT a.aid, a.fornamn, a.efternamn, a.epost "
                    + "FROM anstalld a "
                    + "JOIN ans_proj ap ON a.aid = ap.aid "
                    + "WHERE ap.pid = " + pid + " "
                    + "ORDER BY a.fornamn, a.efternamn";

            ArrayList<HashMap<String, String>> rows = idb.fetchRows(sql);

            if (rows != null) {
                for (HashMap<String, String> r : rows) {
                    String aid = r.get("aid");
                    String namn = r.get("fornamn") + " " + r.get("efternamn");
                    String mail = r.get("epost");
                    model.addElement(aid + " - " + namn + " (" + mail + ")");
                }
            }

            listHandlaggareIProjekt.setModel(model);

        } catch (InfException e) {
            JOptionPane.showMessageDialog(this, "Kunde inte ladda projektets handläggare: " + e.getMessage());
        }
    }

    private void laggTillHandlaggare() {
        String vald = (String) cmbAllaHandlaggare.getSelectedItem();

        if (vald == null || vald.equals("Välj handläggare")) {
            JOptionPane.showMessageDialog(this, "Välj en handläggare först.");
            return;
        }

        String aid = vald.split(" - ")[0].trim();

        try {
            // skydd mot dubletter
            String finns = idb.fetchSingle(
                    "SELECT aid FROM ans_proj WHERE pid = " + pid + " AND aid = " + aid
            );

            if (finns != null) {
                JOptionPane.showMessageDialog(this, "Handläggaren finns redan i projektet.");
                return;
            }

            idb.insert("INSERT INTO ans_proj (pid, aid) VALUES (" + pid + ", " + aid + ")");
            loadHandlaggareIProjekt();

        } catch (InfException e) {
            JOptionPane.showMessageDialog(this, "Fel vid tillägg: " + e.getMessage());
        }
    }

    /**
     * Klick: Ta bort handläggare
     */
    private void taBortHandlaggare() {
        String vald = listHandlaggareIProjekt.getSelectedValue();

        if (vald == null) {
            JOptionPane.showMessageDialog(this, "Välj en handläggare att ta bort.");
            return;
        }

        String aid = vald.split(" - ")[0].trim();

        try {
            idb.delete("DELETE FROM ans_proj WHERE pid = " + pid + " AND aid = " + aid);
            loadHandlaggareIProjekt();

        } catch (InfException e) {
            JOptionPane.showMessageDialog(this, "Fel vid borttagning: " + e.getMessage());
        }
    }

    /**
     * Klick: Tillbaka
     */
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

        jLabel1 = new javax.swing.JLabel();
        cmbAllaHandlaggare = new javax.swing.JComboBox<>();
        btnLaggTillHandlaggare = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        listHandlaggareIProjekt = new javax.swing.JList<>();
        btnTaBortHandlaggare = new javax.swing.JButton();
        btnTillbaka = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel1.setText("Lägg till handläggare:");

        cmbAllaHandlaggare.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cmbAllaHandlaggare.addActionListener(this::cmbAllaHandlaggareActionPerformed);

        btnLaggTillHandlaggare.setText("Lägg till");
        btnLaggTillHandlaggare.addActionListener(this::btnLaggTillHandlaggareActionPerformed);

        jLabel2.setText("Handläggare i projektet:");

        listHandlaggareIProjekt.setModel(new javax.swing.AbstractListModel<String>() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public String getElementAt(int i) { return strings[i]; }
        });
        jScrollPane1.setViewportView(listHandlaggareIProjekt);

        btnTaBortHandlaggare.setText("Ta bort");
        btnTaBortHandlaggare.addActionListener(this::btnTaBortHandlaggareActionPerformed);

        btnTillbaka.setText("Tillbaka");
        btnTillbaka.addActionListener(this::btnTillbakaActionPerformed);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(23, 23, 23)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 141, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 122, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(cmbAllaHandlaggare, javax.swing.GroupLayout.PREFERRED_SIZE, 318, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btnLaggTillHandlaggare)
                            .addComponent(btnTaBortHandlaggare)
                            .addComponent(btnTillbaka))))
                .addContainerGap(23, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(35, 35, 35)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(cmbAllaHandlaggare, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnLaggTillHandlaggare))
                .addGap(18, 18, 18)
                .addComponent(jLabel2)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(29, 29, 29)
                        .addComponent(btnTaBortHandlaggare)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnTillbaka)))
                .addContainerGap(32, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnLaggTillHandlaggareActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLaggTillHandlaggareActionPerformed
        laggTillHandlaggare();
    }//GEN-LAST:event_btnLaggTillHandlaggareActionPerformed

    private void btnTaBortHandlaggareActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTaBortHandlaggareActionPerformed
        taBortHandlaggare();
    }//GEN-LAST:event_btnTaBortHandlaggareActionPerformed

    private void btnTillbakaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTillbakaActionPerformed
        gaTillbaka();
    }//GEN-LAST:event_btnTillbakaActionPerformed

    private void cmbAllaHandlaggareActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbAllaHandlaggareActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cmbAllaHandlaggareActionPerformed

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
        java.awt.EventQueue.invokeLater(() -> new HanteraHandläggare().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnLaggTillHandlaggare;
    private javax.swing.JButton btnTaBortHandlaggare;
    private javax.swing.JButton btnTillbaka;
    private javax.swing.JComboBox<String> cmbAllaHandlaggare;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JList<String> listHandlaggareIProjekt;
    // End of variables declaration//GEN-END:variables
}
