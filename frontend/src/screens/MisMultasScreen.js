import React, { useCallback, useState } from 'react';
import { FlatList, RefreshControl, StyleSheet, Text, TouchableOpacity, View } from 'react-native';
import { useFocusEffect } from '@react-navigation/native';
import { clienteApi } from '../api/endpoints';
import Loading from '../components/Loading';
import ErrorView from '../components/ErrorView';

const palette = { background: '#F9F5FF', surface: '#FFFFFF', primary: '#0846ED', text: '#2B2A51', muted: '#585781', border: '#DCD9FF', danger: '#B41340' };

function money(value) {
  return Number(value || 0).toLocaleString('es-AR', { style: 'currency', currency: 'ARS' });
}

export default function MisMultasScreen({ navigation }) {
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [error, setError] = useState(null);

  const load = useCallback(async () => {
    setError(null);
    try {
      const data = await clienteApi.multas().catch(() => []);
      setItems(data || []);
    } catch (err) {
      setError(err);
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  }, []);

  useFocusEffect(useCallback(() => { load(); }, [load]));

  if (loading) return <Loading text="Cargando multas..." />;
  if (error) return <ErrorView error={error} onRetry={() => { setLoading(true); load(); }} />;

  return (
    <View style={styles.screen}>
      <Text style={styles.title}>Mis multas</Text>
      <FlatList
        contentContainerStyle={styles.list}
        data={items}
        keyExtractor={(item) => String(item.id)}
        refreshControl={<RefreshControl refreshing={refreshing} onRefresh={() => { setRefreshing(true); load(); }} />}
        ListEmptyComponent={<Text style={styles.empty}>No tenés multas pendientes.</Text>}
        renderItem={({ item }) => (
          <View style={styles.card}>
            <Text style={styles.cardTitle}>Multa #{item.id}</Text>
            <Text style={styles.line}>Adquisicion #{item.adquisicionId || 'N/D'}</Text>
            <Text style={styles.line}>{money(item.importe)}</Text>
            <Text style={styles.status}>{String(item.estado || 'pending').toUpperCase()}</Text>
            <TouchableOpacity style={styles.primary} onPress={() => navigation.navigate('Pago', { id: item.id, kind: 'multa' })}>
              <Text style={styles.primaryText}>Pagar</Text>
            </TouchableOpacity>
          </View>
        )}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  screen: { flex: 1, backgroundColor: palette.background, paddingTop: 60, paddingHorizontal: 18 },
  title: { color: palette.text, fontSize: 30, fontWeight: '900', marginBottom: 12 },
  list: { paddingBottom: 20 },
  empty: { textAlign: 'center', color: palette.muted, marginTop: 40 },
  card: { backgroundColor: palette.surface, borderRadius: 18, borderWidth: 1, borderColor: palette.border, padding: 16, marginBottom: 12 },
  cardTitle: { color: palette.text, fontWeight: '900', fontSize: 18 },
  line: { color: palette.muted, marginTop: 6 },
  status: { color: palette.danger, fontWeight: '900', marginTop: 8 },
  primary: { backgroundColor: palette.primary, paddingVertical: 12, borderRadius: 12, alignItems: 'center', marginTop: 12 },
  primaryText: { color: '#fff', fontWeight: '800' },
});