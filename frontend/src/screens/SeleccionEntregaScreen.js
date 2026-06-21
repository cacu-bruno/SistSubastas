import React, { useState } from 'react';
import { Alert, ScrollView, StyleSheet, Text, TextInput, TouchableOpacity, View } from 'react-native';
import { clienteApi } from '../api/endpoints';

const palette = { background: '#F9F5FF', surface: '#FFFFFF', primary: '#0846ED', text: '#2B2A51', muted: '#585781', border: '#DCD9FF' };

export default function SeleccionEntregaScreen({ navigation, route }) {
  const { id } = route.params;
  const [direccion, setDireccion] = useState('');
  const [transportista, setTransportista] = useState('');
  const [codigoSeguimiento, setCodigoSeguimiento] = useState('');
  const [codigoRetiro, setCodigoRetiro] = useState('');
  const [fechaEstimada, setFechaEstimada] = useState('');
  const [costoEnvio, setCostoEnvio] = useState('0');

  async function crearEnvio() {
    try {
      await clienteApi.entregaEnvio(id, {
        direccion,
        transportista,
        codigoSeguimiento,
        fechaEstimada: fechaEstimada || null,
        costoEnvio: Number(costoEnvio || 0),
      });
      navigation.navigate('EstadoEntrega', { id });
    } catch (err) {
      Alert.alert('No se pudo guardar', err.message || 'Ocurrio un error');
    }
  }

  async function crearRetiro() {
    try {
      await clienteApi.entregaRetiro(id, {
        codigoRetiro,
        fechaEstimada: fechaEstimada || null,
      });
      navigation.navigate('EstadoEntrega', { id });
    } catch (err) {
      Alert.alert('No se pudo guardar', err.message || 'Ocurrio un error');
    }
  }

  return (
    <ScrollView contentContainerStyle={styles.container}>
      <Text style={styles.title}>Seleccion de entrega</Text>
      <Text style={styles.subtitle}>Elegí envio o retiro para la compra #{id}.</Text>

      <View style={styles.card}>
        <Text style={styles.cardTitle}>Envio</Text>
        <TextInput style={styles.input} placeholder="Direccion" value={direccion} onChangeText={setDireccion} />
        <TextInput style={styles.input} placeholder="Transportista" value={transportista} onChangeText={setTransportista} />
        <TextInput style={styles.input} placeholder="Codigo seguimiento" value={codigoSeguimiento} onChangeText={setCodigoSeguimiento} />
        <TextInput style={styles.input} placeholder="Fecha estimada YYYY-MM-DD" value={fechaEstimada} onChangeText={setFechaEstimada} />
        <TextInput style={styles.input} placeholder="Costo envio" value={costoEnvio} onChangeText={setCostoEnvio} keyboardType="numeric" />
        <TouchableOpacity style={styles.primary} onPress={crearEnvio}><Text style={styles.primaryText}>Guardar envio</Text></TouchableOpacity>
      </View>

      <View style={styles.card}>
        <Text style={styles.cardTitle}>Retiro</Text>
        <TextInput style={styles.input} placeholder="Codigo retiro" value={codigoRetiro} onChangeText={setCodigoRetiro} />
        <TextInput style={styles.input} placeholder="Fecha estimada YYYY-MM-DD" value={fechaEstimada} onChangeText={setFechaEstimada} />
        <TouchableOpacity style={styles.secondary} onPress={crearRetiro}><Text style={styles.secondaryText}>Guardar retiro</Text></TouchableOpacity>
      </View>
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: { padding: 20, paddingTop: 60, backgroundColor: palette.background },
  title: { color: palette.text, fontSize: 30, fontWeight: '900' },
  subtitle: { color: palette.muted, marginTop: 6, marginBottom: 18 },
  card: { backgroundColor: palette.surface, borderWidth: 1, borderColor: palette.border, borderRadius: 18, padding: 16, marginBottom: 16 },
  cardTitle: { color: palette.text, fontSize: 18, fontWeight: '900', marginBottom: 10 },
  input: { borderWidth: 1, borderColor: palette.border, borderRadius: 12, paddingHorizontal: 12, paddingVertical: 10, marginBottom: 10, color: palette.text },
  primary: { backgroundColor: palette.primary, paddingVertical: 14, borderRadius: 14, alignItems: 'center' },
  primaryText: { color: '#fff', fontWeight: '800' },
  secondary: { borderWidth: 1, borderColor: palette.border, paddingVertical: 14, borderRadius: 14, alignItems: 'center' },
  secondaryText: { color: palette.text, fontWeight: '800' },
});