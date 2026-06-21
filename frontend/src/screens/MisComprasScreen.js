import React, { useCallback, useState } from 'react';
import { FlatList, RefreshControl, StyleSheet, Text, TouchableOpacity, View } from 'react-native';
import { useFocusEffect } from '@react-navigation/native';
import { clienteApi } from '../api/endpoints';
import Loading from '../components/Loading';
import ErrorView from '../components/ErrorView';

const palette = {
  background: '#F9F5FF',
  surface: '#FFFFFF',
  primary: '#0846ED',
  text: '#2B2A51',
  muted: '#585781',
  border: '#DCD9FF',
  accent: '#913983',
};

function money(value) {
  const n = Number(value || 0);
  return n.toLocaleString('es-AR', { style: 'currency', currency: 'ARS' });
}

export default function MisComprasScreen({ navigation }) {
  const [items, setItems] = useState([]);
  const [resumen, setResumen] = useState(null);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [error, setError] = useState(null);

  const load = useCallback(async () => {
    setError(null);
    try {
      const [data, summary] = await Promise.all([
        clienteApi.adquisiciones().catch(() => []),
        clienteApi.adquisicionResumen().catch(() => null),
      ]);
      setItems(data || []);
      setResumen(summary);
    } catch (err) {
      setError(err);
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  }, []);

  useFocusEffect(useCallback(() => { load(); }, [load]));

  if (loading) return <Loading text="Cargando compras..." />;
  if (error) return <ErrorView error={error} onRetry={() => { setLoading(true); load(); }} />;

  return (
    <View style={styles.screen}>
      <View style={styles.header}>
        <Text style={styles.title}>Mis compras</Text>
        <Text style={styles.subtitle}>Seguimiento de adquisiciones, pago y entrega.</Text>
      </View>

      {resumen ? (
        <View style={styles.summary}>
          <View style={styles.summaryItem}><Text style={styles.summaryValue}>{resumen.total}</Text><Text style={styles.summaryLabel}>Total</Text></View>
          <View style={styles.summaryItem}><Text style={styles.summaryValue}>{resumen.pendientes}</Text><Text style={styles.summaryLabel}>Pendientes</Text></View>
          <View style={styles.summaryItem}><Text style={styles.summaryValue}>{resumen.pagadas}</Text><Text style={styles.summaryLabel}>Pagadas</Text></View>
        </View>
      ) : null}

      <FlatList
        contentContainerStyle={styles.list}
        data={items}
        keyExtractor={(item) => String(item.id)}
        refreshControl={<RefreshControl refreshing={refreshing} onRefresh={() => { setRefreshing(true); load(); }} />}
        ListEmptyComponent={<Text style={styles.empty}>Todavía no tenés adquisiciones.</Text>}
        renderItem={({ item }) => (
          <TouchableOpacity style={styles.card} activeOpacity={0.86} onPress={() => navigation.navigate('AdquisicionDetail', { id: item.id })}>
            <View style={styles.cardHeader}>
              <Text style={styles.cardTitle}>Compra #{item.id}</Text>
              <Text style={styles.badge}>{String(item.estado || 'pendiente').toUpperCase()}</Text>
            </View>
            <Text style={styles.line}>{item.producto || 'Producto sin descripcion'}</Text>
            <Text style={styles.line}>{money(item.importe)} + {money(item.comision)}</Text>
            <View style={styles.actions}>
              <TouchableOpacity style={styles.action} onPress={() => navigation.navigate('Factura', { id: item.id })}><Text style={styles.actionText}>Factura</Text></TouchableOpacity>
              <TouchableOpacity style={styles.actionPrimary} onPress={() => navigation.navigate('Pago', { id: item.id })}><Text style={styles.actionTextPrimary}>Pagar</Text></TouchableOpacity>
            </View>
          </TouchableOpacity>
        )}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  screen: { flex: 1, backgroundColor: palette.background, paddingTop: 56, paddingHorizontal: 18 },
  header: { marginBottom: 14 },
  title: { color: palette.text, fontSize: 30, fontWeight: '900' },
  subtitle: { color: palette.muted, marginTop: 6 },
  summary: { flexDirection: 'row', gap: 10, marginBottom: 10 },
  summaryItem: { flex: 1, backgroundColor: palette.surface, borderWidth: 1, borderColor: palette.border, borderRadius: 16, padding: 12 },
  summaryValue: { color: palette.primary, fontSize: 20, fontWeight: '900' },
  summaryLabel: { color: palette.muted, fontSize: 11, fontWeight: '800', marginTop: 4, textTransform: 'uppercase' },
  list: { paddingBottom: 24 },
  empty: { textAlign: 'center', color: palette.muted, marginTop: 40 },
  card: { backgroundColor: palette.surface, borderRadius: 18, borderWidth: 1, borderColor: palette.border, padding: 16, marginBottom: 12 },
  cardHeader: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center' },
  cardTitle: { color: palette.text, fontSize: 18, fontWeight: '900' },
  badge: { color: palette.accent, fontWeight: '900', fontSize: 11 },
  line: { color: palette.muted, marginTop: 6 },
  actions: { flexDirection: 'row', gap: 10, marginTop: 14 },
  action: { flex: 1, borderWidth: 1, borderColor: palette.border, borderRadius: 12, paddingVertical: 10, alignItems: 'center' },
  actionPrimary: { flex: 1, backgroundColor: palette.primary, borderRadius: 12, paddingVertical: 10, alignItems: 'center' },
  actionText: { color: palette.text, fontWeight: '800' },
  actionTextPrimary: { color: '#fff', fontWeight: '800' },
});