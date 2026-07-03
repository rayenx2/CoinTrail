import { useState, useEffect } from 'react';
import { recurringService, categoryService } from '../services/api';
import { Plus, Repeat, Trash2, Calendar } from 'lucide-react';
import { motion, AnimatePresence } from 'framer-motion';
import toast from 'react-hot-toast';

export default function Recurring() {
  const [rules, setRules] = useState([]);
  const [categories, setCategories] = useState([]);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchRules();
    fetchCategories();
  }, []);

  const fetchCategories = async () => {
    try {
      const { data } = await categoryService.getAll();
      setCategories(data);
    } catch (err) {
      console.error(err);
    }
  };

  const fetchRules = async () => {
    try {
      const { data } = await recurringService.getAll();
      setRules(data);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleCreate = async (payload) => {
    try {
      await recurringService.create(payload);
      toast.success('Recurring transaction created');
      setIsModalOpen(false);
      fetchRules();
    } catch (err) {
      toast.error(err.response?.data?.message || 'Could not create recurring transaction');
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Stop this recurring transaction? It will no longer auto-generate.')) return;
    try {
      await recurringService.delete(id);
      toast.success('Recurring transaction stopped');
      fetchRules();
    } catch (err) {
      toast.error('Could not delete recurring transaction');
    }
  };

  return (
    <motion.div initial={{ opacity: 0, y: 20 }} animate={{ opacity: 1, y: 0 }} className="space-y-10 pb-20">
      <div className="flex flex-col md:flex-row justify-between items-start md:items-center gap-6">
        <div>
          <h1 className="text-4xl font-black text-foreground tracking-tight mb-2">Recurring</h1>
          <p className="text-muted text-lg font-medium">Rent, subscriptions, salary — set it once, it logs itself every month.</p>
        </div>
        <button onClick={() => setIsModalOpen(true)} className="btn-primary flex items-center gap-2">
          <Repeat className="w-5 h-5" /> New Recurring Transaction
        </button>
      </div>

      <RecurringModal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        onSave={handleCreate}
        categories={categories}
      />

      <div className="glass-card overflow-hidden shadow-xl border border-border">
        <div className="overflow-x-auto">
          <table className="w-full text-left border-collapse">
            <thead>
              <tr className="bg-secondary/50 border-b border-border">
                <th className="px-8 py-5 text-[10px] font-black uppercase tracking-[0.2em] text-muted">Description</th>
                <th className="px-8 py-5 text-[10px] font-black uppercase tracking-[0.2em] text-muted">Category</th>
                <th className="px-8 py-5 text-[10px] font-black uppercase tracking-[0.2em] text-muted">Runs on</th>
                <th className="px-8 py-5 text-[10px] font-black uppercase tracking-[0.2em] text-muted">Last generated</th>
                <th className="px-8 py-5 text-[10px] font-black uppercase tracking-[0.2em] text-muted text-right">Amount</th>
                <th className="px-8 py-5"></th>
              </tr>
            </thead>
            <tbody className="divide-y divide-border/50">
              <AnimatePresence>
                {rules.map((r) => (
                  <motion.tr
                    key={r.id}
                    initial={{ opacity: 0 }}
                    animate={{ opacity: 1 }}
                    className="hover:bg-primary/[0.02] transition-colors group"
                  >
                    <td className="px-8 py-5">
                      <p className="font-extrabold text-foreground">{r.description || 'Recurring transaction'}</p>
                    </td>
                    <td className="px-8 py-5">
                      <span className="px-3 py-1 bg-secondary border border-border rounded-lg text-[10px] font-black text-muted uppercase tracking-wider">
                        {r.categoryIcon} {r.categoryName}
                      </span>
                    </td>
                    <td className="px-8 py-5">
                      <div className="flex items-center gap-2 text-muted text-xs font-bold">
                        <Calendar className="w-3.5 h-3.5 opacity-40" /> Day {r.dayOfMonth} of every month
                      </div>
                    </td>
                    <td className="px-8 py-5 text-muted text-xs font-bold">
                      {r.lastGeneratedPeriod || 'Not yet run'}
                    </td>
                    <td className="px-8 py-5 text-right">
                      <p className={`text-lg font-black tracking-tight ${r.type === 'INCOME' ? 'text-emerald-500' : 'text-foreground'}`}>
                        {r.type === 'INCOME' ? '+' : '-'} €{r.amount?.toLocaleString()}
                      </p>
                    </td>
                    <td className="px-8 py-5 text-right">
                      <button
                        onClick={() => handleDelete(r.id)}
                        className="p-2.5 hover:bg-rose-500/10 rounded-xl text-muted hover:text-rose-500 transition-all opacity-0 group-hover:opacity-100"
                      >
                        <Trash2 className="w-4 h-4" />
                      </button>
                    </td>
                  </motion.tr>
                ))}
              </AnimatePresence>
            </tbody>
          </table>

          {rules.length === 0 && !loading && (
            <div className="py-24 text-center">
              <div className="w-20 h-20 bg-secondary rounded-[24px] flex items-center justify-center mx-auto mb-6 border border-border">
                <Repeat className="w-8 h-8 text-muted" />
              </div>
              <h3 className="text-xl font-bold text-foreground mb-1">No recurring transactions yet</h3>
              <p className="text-muted text-sm font-medium max-w-xs mx-auto">
                Add rent, a subscription, or your salary and it'll log itself automatically every month.
              </p>
            </div>
          )}
        </div>
      </div>
    </motion.div>
  );
}

function RecurringModal({ isOpen, onClose, onSave, categories }) {
  const [formData, setFormData] = useState({
    categoryId: '', amount: '', description: '', type: 'EXPENSE', dayOfMonth: 1
  });

  if (!isOpen) return null;

  const handleSubmit = (e) => {
    e.preventDefault();
    onSave({
      ...formData,
      amount: Number(formData.amount),
      categoryId: Number(formData.categoryId),
      dayOfMonth: Number(formData.dayOfMonth)
    });
    setFormData({ categoryId: '', amount: '', description: '', type: 'EXPENSE', dayOfMonth: 1 });
  };

  return (
    <AnimatePresence>
      <div className="fixed inset-0 z-[100] flex items-center justify-center p-4">
        <motion.div
          initial={{ opacity: 0 }} animate={{ opacity: 1 }} exit={{ opacity: 0 }}
          onClick={onClose}
          className="absolute inset-0 bg-[#020617]/80 backdrop-blur-md"
        />
        <motion.div
          initial={{ opacity: 0, scale: 0.95, y: 20 }}
          animate={{ opacity: 1, scale: 1, y: 0 }}
          exit={{ opacity: 0, scale: 0.95, y: 20 }}
          className="relative w-full max-w-lg glass rounded-[40px] shadow-2xl p-8 border border-border"
        >
          <div className="flex justify-between items-center mb-10">
            <div>
              <h2 className="text-3xl font-black text-foreground tracking-tighter">New Recurring Transaction</h2>
              <p className="text-muted text-sm mt-1 uppercase tracking-widest font-bold">Auto-logs every month</p>
            </div>
          </div>

          <form onSubmit={handleSubmit} className="space-y-6">
            <div className="flex p-1.5 bg-secondary rounded-2xl border border-border">
              <button
                type="button"
                onClick={() => setFormData({ ...formData, type: 'EXPENSE' })}
                className={`flex-1 py-4 rounded-xl font-bold transition-all ${formData.type === 'EXPENSE' ? 'bg-rose-500 text-white shadow-lg' : 'text-muted hover:text-foreground'}`}
              >
                Expense
              </button>
              <button
                type="button"
                onClick={() => setFormData({ ...formData, type: 'INCOME' })}
                className={`flex-1 py-4 rounded-xl font-bold transition-all ${formData.type === 'INCOME' ? 'bg-emerald-500 text-white shadow-lg' : 'text-muted hover:text-foreground'}`}
              >
                Income
              </button>
            </div>

            <input
              type="number" step="0.01" required placeholder="Amount"
              value={formData.amount}
              onChange={(e) => setFormData({ ...formData, amount: e.target.value })}
              className="auth-input h-14 w-full"
            />

            <select
              required
              value={formData.categoryId}
              onChange={(e) => setFormData({ ...formData, categoryId: e.target.value })}
              className="auth-input h-14 w-full bg-[#1e293b]"
            >
              <option value="">Select Category</option>
              {categories.map(c => <option key={c.id} value={c.id}>{c.name}</option>)}
            </select>

            <input
              type="text" placeholder="Description (e.g. Rent, Netflix)"
              value={formData.description}
              onChange={(e) => setFormData({ ...formData, description: e.target.value })}
              className="auth-input h-14 w-full"
            />

            <div className="space-y-2">
              <label className="text-[10px] font-black text-muted uppercase tracking-[0.2em] px-2">Day of month it runs (1–28)</label>
              <input
                type="number" min="1" max="28" required
                value={formData.dayOfMonth}
                onChange={(e) => setFormData({ ...formData, dayOfMonth: e.target.value })}
                className="auth-input h-14 w-full"
              />
            </div>

            <div className="flex gap-3">
              <button type="button" onClick={onClose} className="flex-1 py-4 rounded-2xl border border-border text-muted font-bold hover:bg-secondary transition-all">
                Cancel
              </button>
              <button type="submit" className="flex-1 btn-primary py-4 rounded-2xl font-black flex items-center justify-center gap-2">
                <Plus className="w-5 h-5" /> Create
              </button>
            </div>
          </form>
        </motion.div>
      </div>
    </AnimatePresence>
  );
}
