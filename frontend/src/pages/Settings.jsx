import { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { userService } from '../services/api';
import { User, Shield, Save } from 'lucide-react';
import { motion } from 'framer-motion';
import toast from 'react-hot-toast';

const CURRENCIES = [
  { value: 'EUR', label: 'EUR - Euro (€)' },
  { value: 'USD', label: 'USD - US Dollar ($)' },
  { value: 'GBP', label: 'GBP - British Pound (£)' },
  { value: 'CHF', label: 'CHF - Swiss Franc (CHF)' },
];

export default function Settings() {
  const { user, updateUser } = useAuth();
  const [activeTab, setActiveTab] = useState('profile');
  const [name, setName] = useState(user?.name || '');
  const [currency, setCurrency] = useState('EUR');
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    userService.getMe().then(({ data }) => {
      setName(data.name);
      setCurrency(data.currency);
    }).catch(() => {});
  }, []);

  const sections = [
    { id: 'profile', name: 'Profile', icon: <User className="w-5 h-5" /> },
    { id: 'security', name: 'Security', icon: <Shield className="w-5 h-5" /> },
  ];

  const handleSaveProfile = async () => {
    setSaving(true);
    try {
      const { data } = await userService.updateProfile({ name, currency });
      updateUser({ name: data.name, currency: data.currency });
      toast.success('Profile updated');
    } catch (err) {
      toast.error(err.response?.data?.message || 'Could not save profile');
    } finally {
      setSaving(false);
    }
  };

  return (
    <motion.div
      initial={{ opacity: 0, scale: 0.98 }}
      animate={{ opacity: 1, scale: 1 }}
      className="pb-20"
    >
      <div className="flex flex-col md:flex-row justify-between items-start md:items-center gap-6 mb-12">
        <div>
          <h1 className="text-4xl font-black text-foreground tracking-tight mb-2">Settings</h1>
          <p className="text-muted text-lg font-medium">Manage your profile and account security.</p>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-4 gap-10">
        <div className="lg:col-span-1 space-y-2">
          {sections.map(s => (
            <button
              key={s.id}
              onClick={() => setActiveTab(s.id)}
              className={`w-full flex items-center gap-4 px-6 py-4 rounded-2xl transition-all font-bold text-sm tracking-tight uppercase ${
                activeTab === s.id
                  ? 'bg-primary text-primary-foreground shadow-lg shadow-primary/20'
                  : 'text-muted hover:text-foreground hover:bg-secondary border border-transparent'
              }`}
            >
              {s.icon} {s.name}
            </button>
          ))}
        </div>

        <div className="lg:col-span-3 space-y-8">
          {activeTab === 'profile' && (
            <div className="glass-card p-8 md:p-10">
              <h3 className="text-2xl font-bold text-foreground mb-8">Personal Information</h3>

              <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
                <div className="space-y-3">
                  <label className="text-xs font-black text-muted uppercase tracking-widest px-1">Full Name</label>
                  <input
                    type="text"
                    value={name}
                    onChange={(e) => setName(e.target.value)}
                    className="w-full bg-secondary border border-border rounded-xl px-5 h-14 text-foreground focus:outline-none focus:ring-2 focus:ring-primary/50 focus:border-primary transition-all font-medium"
                    placeholder="Enter your name"
                  />
                </div>
                <div className="space-y-3">
                  <label className="text-xs font-black text-muted uppercase tracking-widest px-1">Email Address</label>
                  <input
                    type="email"
                    value={user?.email || ''}
                    className="w-full bg-secondary/50 border border-border rounded-xl px-5 h-14 text-muted cursor-not-allowed font-medium"
                    disabled
                  />
                </div>
                <div className="md:col-span-2 space-y-3">
                  <label className="text-xs font-black text-muted uppercase tracking-widest px-1">Default Currency</label>
                  <select
                    value={currency}
                    onChange={(e) => setCurrency(e.target.value)}
                    className="w-full bg-secondary border border-border rounded-xl px-5 h-14 text-foreground focus:outline-none focus:ring-2 focus:ring-primary/50 focus:border-primary transition-all font-medium appearance-none"
                  >
                    {CURRENCIES.map(c => (
                      <option key={c.value} value={c.value}>{c.label}</option>
                    ))}
                  </select>
                </div>
              </div>

              <button
                onClick={handleSaveProfile}
                disabled={saving}
                className="btn-primary flex items-center gap-3 px-8 mt-8 shadow-primary/20 shadow-lg active:scale-95"
              >
                {saving ? (
                  <div className="w-5 h-5 border-2 border-primary-foreground/20 border-t-primary-foreground rounded-full animate-spin" />
                ) : (
                  <Save className="w-5 h-5" />
                )}
                {saving ? 'Saving...' : 'Save Changes'}
              </button>
            </div>
          )}

          {activeTab === 'security' && <ChangePasswordCard />}
        </div>
      </div>
    </motion.div>
  );
}

function ChangePasswordCard() {
  const [currentPassword, setCurrentPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSubmitting(true);
    try {
      await userService.changePassword({ currentPassword, newPassword });
      toast.success('Password changed');
      setCurrentPassword('');
      setNewPassword('');
    } catch (err) {
      toast.error(err.response?.status === 400 ? 'Current password is incorrect' : 'Could not change password');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="glass-card p-8 md:p-10">
      <h3 className="text-2xl font-bold text-foreground mb-2">Change Password</h3>
      <p className="text-muted font-medium text-sm mb-8">Update your account password.</p>

      <form onSubmit={handleSubmit} className="space-y-6 max-w-md">
        <div className="space-y-3">
          <label className="text-xs font-black text-muted uppercase tracking-widest px-1">Current Password</label>
          <input
            type="password"
            required
            value={currentPassword}
            onChange={(e) => setCurrentPassword(e.target.value)}
            className="w-full bg-secondary border border-border rounded-xl px-5 h-14 text-foreground focus:outline-none focus:ring-2 focus:ring-primary/50 focus:border-primary transition-all font-medium"
          />
        </div>
        <div className="space-y-3">
          <label className="text-xs font-black text-muted uppercase tracking-widest px-1">New Password</label>
          <input
            type="password"
            required
            minLength={8}
            value={newPassword}
            onChange={(e) => setNewPassword(e.target.value)}
            className="w-full bg-secondary border border-border rounded-xl px-5 h-14 text-foreground focus:outline-none focus:ring-2 focus:ring-primary/50 focus:border-primary transition-all font-medium"
          />
        </div>
        <button
          type="submit"
          disabled={submitting}
          className="btn-primary flex items-center gap-3 px-8 shadow-primary/20 shadow-lg active:scale-95"
        >
          {submitting ? 'Updating...' : 'Update Password'}
        </button>
      </form>
    </div>
  );
}
